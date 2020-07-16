/*
 * Copyright 2020 Hazelcast Inc.
 *
 * Licensed under the Hazelcast Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://hazelcast.com/hazelcast-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.hazelcast.session;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Session;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

public class HazelcastSessionManager extends HazelcastSessionManagerBase implements Lifecycle, PropertyChangeListener {

    private static final String NAME = "HazelcastSessionManager";

    private static final int DEFAULT_SESSION_TIMEOUT = 60;

    protected LifecycleSupport lifecycle = new LifecycleSupport(this);

    private final Log log = LogFactory.getLog(HazelcastSessionManager.class);

    public void setSessionTimeout(int t) {
        getContext().setSessionTimeout(t);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void load() {
    }

    @Override
    public void unload() {
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

        String mapNameToUse;
        if (getMapName() == null || "default".equals(getMapName())) {
            Context ctx = getContext();
            String contextPath = ctx.getServletContext().getContextPath();
            log.debug("contextPath: " + contextPath);
            if (contextPath == null || contextPath.equals("/") || contextPath.equals("")) {
                mapNameToUse = "empty_session_replication";
            } else {
                mapNameToUse = contextPath.substring(1) + "_session_replication";
            }
        } else {
            mapNameToUse = getMapName();
        }

        startHZClient(getContext().getLoader().getClassLoader(), mapNameToUse);
        
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
            stopHZClient();
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
        session.setMaxInactiveIntervalLocal(getMaxInactiveInterval());

        String newSessionId = sessionId;
        if (newSessionId == null) {
            newSessionId = generateSessionId();
        }

        session.setId(newSessionId);
        session.tellNew();

        sessions.put(newSessionId, session);
        getDistributedMap().set(newSessionId, session);
        return session;
    }

    @Override
    public Session createEmptySession() {
        return new HazelcastSession(this);
    }

    @Override
    public void add(Session session) {
        sessions.put(session.getId(), session);
        getDistributedMap().set(session.getId(), (HazelcastSession) session);
    }

    @Override
    public Session findSession(String id) {
        log.debug("sessionId: " + id);
        if (id == null) {
            return null;
        }

        if (!isSticky() || (isSticky() && !sessions.containsKey(id))) {
            if (isSticky()) {
                log.debug("Sticky Session is currently enabled. "
                        + "Some failover occurred so reading session from Hazelcast map: " + getMapName());
            }

            HazelcastSession hazelcastSession = getDistributedMap().get(id);
            if (hazelcastSession == null) {
                log.debug("No Session found for: " + id);
                return null;
            }

            hazelcastSession.access();
            hazelcastSession.endAccess();

            hazelcastSession.setSessionManager(this);

            sessions.put(id, hazelcastSession);

            // call remove method to trigger eviction Listener on each node to invalidate local sessions
            // the call are performed in a pessimistic lock block to prevent concurrency problems whilst finding sessions
            getDistributedMap().lock(id);
            try {
                getDistributedMap().remove(id);
                getDistributedMap().set(id, hazelcastSession);
            } finally {
                getDistributedMap().unlock(id);
            }

            return hazelcastSession;
        } else {
            return sessions.get(id);
        }
    }

    public void commit(Session session) {
        HazelcastSession hazelcastSession = (HazelcastSession) session;
        if (hazelcastSession.isDirty()) {
            hazelcastSession.setDirty(false);
            getDistributedMap().set(session.getId(), hazelcastSession);
            if (log.isDebugEnabled()) {
                log.debug("Thread name: " + Thread.currentThread().getName() + " committed key: " + session.getId());
            }
        }
    }

    @Override
    public String updateJvmRouteForSession(String sessionId, String newJvmRoute) {
        HazelcastSession session = getDistributedMap().get(sessionId);
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

        getDistributedMap().remove(sessionId);
        getDistributedMap().set(newSessionId, session);
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

    private void remove(String id) {
        sessions.remove(id);
        getDistributedMap().remove(id);
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

    private void checkMaxActiveSessions() {
        if (getMaxActiveSessions() >= 0 && getDistributedMap().size() >= getMaxActiveSessions()) {
            rejectedSessions++;
            throw new IllegalStateException(sm.getString("managerBase.createSession.ise"));
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

}
