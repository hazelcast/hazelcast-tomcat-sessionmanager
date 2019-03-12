/*
 * Copyright (c) 2008-2019, Hazelcast, Inc. All Rights Reserved.
 */

package com.hazelcast.session;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapEvent;
import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Session;
import org.apache.catalina.session.ManagerBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class HazelcastSessionManager extends ManagerBase implements Lifecycle, PropertyChangeListener, SessionManager {

    private static final String NAME = "HazelcastSessionManager";

    private static final int DEFAULT_SESSION_TIMEOUT = 60;

    private static final int SECONDS_IN_MINUTE = 60;

    private final Log log = LogFactory.getLog(HazelcastSessionManager.class);

    private IMap<String, HazelcastSession> sessionMap;

    private boolean clientOnly;

    private boolean sticky = true;

    private String mapName;

    private boolean deferredWrite = true;

    private String hazelcastInstanceName;

    private HazelcastInstance instance;

    public void setSessionTimeout(int t) {
        getContext().setSessionTimeout(t);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void load() throws ClassNotFoundException, IOException {
    }

    @Override
    public void unload() throws IOException {
    }

    @Override
    public void startInternal() throws LifecycleException {
        super.startInternal();
        super.generateSessionId();

        configureValves();

        instance = HazelcastInstanceFactory.
                getHazelcastInstance(getContext().getLoader().getClassLoader(), isClientOnly(), getHazelcastInstanceName());

        if (getMapName() == null || "default".equals(getMapName())) {
            Context ctx = getContext();
            String contextPath = ctx.getServletContext().getContextPath();
            log.debug("contextPath: " + contextPath);
            if (contextPath == null || contextPath.equals("/") || contextPath.equals("")) {
                mapName = "empty_session_replication";
            } else {
                mapName = contextPath.substring(1, contextPath.length()) + "_session_replication";
            }
            sessionMap = instance.getMap(mapName);
        } else {
            sessionMap = instance.getMap(getMapName());
        }

        if (!isSticky()) {
            sessionMap.addEntryListener(new EntryListener<String, HazelcastSession>() {
                public void entryAdded(EntryEvent<String, HazelcastSession> event) {
                }

                public void entryRemoved(EntryEvent<String, HazelcastSession> entryEvent) {
                    if (entryEvent.getMember() == null || !entryEvent.getMember().localMember()) {
                        sessions.remove(entryEvent.getKey());
                    }
                }

                public void entryUpdated(EntryEvent<String, HazelcastSession> event) {
                }

                public void entryEvicted(EntryEvent<String, HazelcastSession> entryEvent) {
                    entryRemoved(entryEvent);
                }

                public void mapEvicted(MapEvent event) {
                }

                public void mapCleared(MapEvent event) {
                }
            }, false);
        }

        log.info("HazelcastSessionManager started...");
        setState(LifecycleState.STARTING);
    }

    private void configureValves() {
        if (isSticky()) {
            HazelcastSessionChangeValve hazelcastSessionChangeValve = new HazelcastSessionChangeValve(this);
            hazelcastSessionChangeValve.setAsyncSupported(true);
            getContext().getPipeline().addValve(hazelcastSessionChangeValve);
        }

        if (isDeferredEnabled()) {
            HazelcastSessionCommitValve hazelcastSessionCommitValve = new HazelcastSessionCommitValve(this);
            hazelcastSessionCommitValve.setAsyncSupported(true);
            getContext().getPipeline().addValve(hazelcastSessionCommitValve);
        }
    }

    @Override
    public void stopInternal() throws LifecycleException {
        log.info("stopping HazelcastSessionManager...");

        setState(LifecycleState.STOPPING);
        if (isClientOnly()) {
            instance.shutdown();
        }
        super.stopInternal();
        log.info("HazelcastSessionManager stopped...");
    }

    @Override
    public int getRejectedSessions() {
        // Essentially do nothing.
        return 0;
    }

    public void setRejectedSessions(int i) {
        // Do nothing.
    }

    @Override
    public Session createSession(String sessionId) {
        checkMaxActiveSessions();
        HazelcastSession session = (HazelcastSession) createEmptySession();

        session.setNew(true);
        session.setValid(true);
        session.setCreationTime(System.currentTimeMillis());
        session.setMaxInactiveInterval(getContext().getSessionTimeout() * SECONDS_IN_MINUTE);

        String newSessionId = sessionId;
        if (newSessionId == null) {
            newSessionId = generateSessionId();
        }

        session.setId(newSessionId);
        session.tellNew();

        sessions.put(newSessionId, session);
        sessionMap.set(newSessionId, session);
        return session;
    }

    @Override
    public Session createEmptySession() {
        return new HazelcastSession(this);
    }

    @Override
    public void add(Session session) {
        sessions.put(session.getId(), (HazelcastSession) session);
        sessionMap.set(session.getId(), (HazelcastSession) session);
    }

    @Override
    public Session findSession(String id) throws IOException {
        log.debug("Attempting to find sessionId: " + id);

        if (id == null) {
            return null;
        }

        if (!isSticky() || (isSticky() && !sessions.containsKey(id))) {
            if (isSticky()) {
                log.debug("Sticky Session is currently enabled. "
                        + "Some failover occurred so reading session from Hazelcast map: " + getMapName());
            }

            HazelcastSession hazelcastSession = sessionMap.get(id);
            if (hazelcastSession == null) {
                log.debug("No Session found for: " + id);
                return null;
            }

            log.debug("Session found for: " + id);

            hazelcastSession.access();
            hazelcastSession.endAccess();

            hazelcastSession.setSessionManager(this);

            sessions.put(id, hazelcastSession);

            // call remove method to trigger eviction Listener on each node to invalidate local sessions
            sessionMap.remove(id);
            sessionMap.set(id, hazelcastSession);

            return hazelcastSession;
        } else {
            return sessions.get(id);
        }
    }

    @Override
    public Session[] findSessions() {
        // Get all local sessions
        Set<Session> allSessions = new HashSet<Session>(sessions.values());

        // Get all non-local sessions ids
        Set<String> keys = new HashSet<String>(sessionMap.keySet());
        keys.removeAll(sessions.keySet());

        // Get all non-local sessions
        final Collection<HazelcastSession> nonLocalSessions = sessionMap.getAll(keys).values();

        // Set SessionManager since it's a transient field
        for (HazelcastSession nonLocalSession : nonLocalSessions) {
            if (nonLocalSession.getManager() == null) {
                nonLocalSession.setSessionManager(this);
            }
        }

        // Add all non-local sessions
        allSessions.addAll(nonLocalSessions);

        return allSessions.toArray(new Session[allSessions.size()]);
    }

    public void commit(Session session) {
        HazelcastSession hazelcastSession = (HazelcastSession) session;
        if (hazelcastSession.isDirty()) {
            hazelcastSession.setDirty(false);
            sessionMap.set(session.getId(), hazelcastSession);
            if (log.isDebugEnabled()) {
                log.debug("Thread name: " + Thread.currentThread().getName() + " committed key: " + session.getId());
            }
        }
    }

    @Override
    public String updateJvmRouteForSession(String sessionId, String newJvmRoute) throws IOException {
        HazelcastSession session = sessionMap.get(sessionId);
        if (session == null) {
            session = (HazelcastSession) createSession(null);
            return session.getId();
        }

        if (session.getManager() == null) {
            session.setSessionManager(this);
        }
        int index = sessionId.indexOf(".");
        String baseSessionId = sessionId.substring(0, index);
        String newSessionId = baseSessionId + "." + newJvmRoute;
        session.setId(newSessionId);

        sessionMap.remove(sessionId);
        sessionMap.set(newSessionId, session);
        return newSessionId;
    }

    @Override
    public void remove(Session session) {
        remove(session.getId());
        log.debug("Removed session: " + session.getId());
    }

    @Override
    public void remove(Session session, boolean update) {
       remove(session);
    }

    @Override
    public IMap<String, HazelcastSession> getDistributedMap() {
        return sessionMap;
    }

    @Override
    public boolean isDeferredEnabled() {
        return deferredWrite;
    }

    public boolean isClientOnly() {
        return clientOnly;
    }

    public void setClientOnly(boolean clientOnly) {
        this.clientOnly = clientOnly;
    }

    public boolean isSticky() {
        return sticky;
    }

    public void setSticky(boolean sticky) {
        if (!sticky && getJvmRoute() != null) {
            log.warn("setting JvmRoute with non-sticky sessions is not recommended and might cause unstable behaivour");
        }
        this.sticky = sticky;
    }

    private void remove(String id) {
        sessions.remove(id);
        sessionMap.remove(id);
    }

    @Override
    public void expireSession(String sessionId) {
        super.expireSession(sessionId);
        remove(sessionId);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("sessionTimeout")) {
            getContext().setSessionTimeout((Integer) evt.getNewValue() * DEFAULT_SESSION_TIMEOUT);
        }
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public String getHazelcastInstanceName() {
        return hazelcastInstanceName;
    }

    public void setHazelcastInstanceName(String hazelcastInstanceName) {
        this.hazelcastInstanceName = hazelcastInstanceName;
    }

    private void checkMaxActiveSessions() {
        if (getMaxActiveSessions() >= 0 && sessionMap.size() >= getMaxActiveSessions()) {
            rejectedSessions++;
            throw new IllegalStateException(sm.getString("standardManager.createSession.ise"));
        }
    }

    public int getMaxActiveSessions() {
        return this.maxActiveSessions;
    }

    public void setMaxActiveSessions(int maxActiveSessions) {
        int oldMaxActiveSessions = this.maxActiveSessions;
        this.maxActiveSessions = maxActiveSessions;
        this.support.firePropertyChange("maxActiveSessions", Integer.valueOf(oldMaxActiveSessions),
                Integer.valueOf(this.maxActiveSessions));
    }

    public void setDeferredWrite(boolean deferredWrite) {
        this.deferredWrite = deferredWrite;
    }
}
