package com.hazelcast.session;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapEvent;
import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Session;
import org.apache.catalina.session.ManagerBase;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;


public class HazelcastSessionManager extends ManagerBase implements Lifecycle, PropertyChangeListener, SessionManager {

    private static final String NAME = "HazelcastSessionManager";
    private static final String INFO = "HazelcastSessionManager/1.0";

    private static final int DEFAULT_SESSION_TIMEOUT = 60;

    protected LifecycleSupport lifecycle = new LifecycleSupport(this);


    private final Log log = LogFactory.getLog(HazelcastSessionManager.class);

    private IMap<String, HazelcastSession> sessionMap;

    private boolean clientOnly;

    private boolean sticky = true;

    private String mapName;

    private boolean deferredWrite = true;

    private HazelcastInstance instance;

    @Override
    public String getInfo() {
        return INFO;
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
    public void addLifecycleListener(LifecycleListener listener) {
        lifecycle.addLifecycleListener(listener);
    }

    @Override
    public LifecycleListener[] findLifecycleListeners() {
        return lifecycle.findLifecycleListeners();
    }

    @Override
    public void removeLifecycleListener(LifecycleListener listener) {
        lifecycle.removeLifecycleListener(listener);
    }

    @Override
    public void startInternal() throws LifecycleException {

        super.startInternal();

        super.generateSessionId();

        configureValves();

        if (isClientOnly()) {
            try {
                instance = HazelcastClient.newHazelcastClient(ClientServerLifecycleListener.getConfig());
            } catch (Exception e) {
                log.error("Hazelcast Client could not be created. ", e);
                throw new LifecycleException(e.getMessage());
            }
        } else {
            instance = Hazelcast.getOrCreateHazelcastInstance(P2PLifecycleListener.getConfig());
        }
        if (getMapName() == null || "default".equals(getMapName())) {
            Context ctx = (Context) getContainer();
            String contextPath = ctx.getServletContext().getContextPath();
            log.info("contextPath:" + contextPath);
            String mapName;
            if (contextPath == null || contextPath.equals("/") || contextPath.equals("")) {
                mapName = "empty_session_replication";
            } else {
                mapName = contextPath.substring(1, contextPath.length())  + "_session_replication";
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
            getContainer().getPipeline().addValve(hazelcastSessionChangeValve);
        }

        if (isDeferredEnabled()) {
            HazelcastSessionCommitValve hazelcastSessionCommitValve = new HazelcastSessionCommitValve(this);
            getContainer().getPipeline().addValve(hazelcastSessionCommitValve);
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
        session.setMaxInactiveInterval(getMaxInactiveInterval());

        String newSessionId = sessionId;
        if (newSessionId == null) {
            newSessionId = generateSessionId();
        }

        session.setId(newSessionId);
        session.tellNew();

        sessions.put(newSessionId, session);
        sessionMap.put(newSessionId, session);
        return session;
    }

    @Override
    public Session createEmptySession() {
        return new HazelcastSession(this);
    }

    @Override
    public void add(Session session) {
        sessions.put(session.getId(), (HazelcastSession) session);
        sessionMap.put(session.getId(), (HazelcastSession) session);
    }

    @Override
    public Session findSession(String id) throws IOException {
        log.debug("sessionId:" + id);
        if (id == null) {
            return null;
        }

        if (!isSticky() || (isSticky() && !sessions.containsKey(id))) {

            if (isSticky()) {
                log.info("Sticky Session is currently enabled."
                        + "Some failover occured so reading session from Hazelcast map:" + getMapName());
            }

        HazelcastSession hazelcastSession = sessionMap.get(id);
        if (hazelcastSession == null) {
            log.info("No Session found for:" + id);
            return null;
        }

        hazelcastSession.access();
        hazelcastSession.endAccess();

        hazelcastSession.setSessionManager(this);

        sessions.put(id, hazelcastSession);

        // call remove method to trigger eviction Listener on each node to invalidate local sessions
        sessionMap.remove(id);
        sessionMap.put(id, hazelcastSession);

        return hazelcastSession;
        } else {
            return sessions.get(id);

        }

    }

    public void commit(Session session) {

        HazelcastSession hazelcastSession = (HazelcastSession) session;

        if (hazelcastSession.isDirty()) {
            hazelcastSession.setDirty(false);
            sessionMap.put(session.getId(), hazelcastSession);
            log.info("Thread name:" + Thread.currentThread().getName() + " commited key:" + hazelcastSession.getAttribute("key"));
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
        sessionMap.put(newSessionId, session);
        return newSessionId;
    }

    @Override
    public void remove(Session session) {
        remove(session.getId());
    }

    @Override
    public void remove(Session session, boolean update) {
        remove(session.getId());
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
            setMaxInactiveInterval((Integer) evt.getNewValue() * DEFAULT_SESSION_TIMEOUT);
        }

    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
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
        this.support.firePropertyChange("maxActiveSessions",
                Integer.valueOf(oldMaxActiveSessions), Integer.valueOf(this.maxActiveSessions));
    }

    public void setDeferredWrite(boolean deferredWrite) {
        this.deferredWrite = deferredWrite;
    }


}
