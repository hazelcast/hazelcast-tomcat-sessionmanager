/*
 * Copyright (c) 2008-2016, Hazelcast, Inc. All Rights Reserved.
 */

package com.hazelcast.session.txsupport;

import com.hazelcast.core.IMap;
import com.hazelcast.session.HazelcastSession;

/**
 * Default Implementation of {@link MapWriteStrategy} encapsulating write access to the Hazelcast Tomcat Session Map.
 * This implementation accesses the session map ({@link IMap}) directly with no transaction support.
 */
public class DefaultMapWriteStrategy implements MapWriteStrategy {

    private final IMap<String, HazelcastSession> sessionMap;

    public DefaultMapWriteStrategy(IMap<String, HazelcastSession> sessionMap) {
        this.sessionMap = sessionMap;
    }

    @Override
    public void setSession(String sessionId, HazelcastSession session) {
        sessionMap.set(sessionId, session);
    }

    @Override
    public void removeSession(String sessionId) {
        sessionMap.remove(sessionId);
    }

    @Override
    public void removeAndSetSession(String existingSessionId, String sessionId, HazelcastSession session) {
        if (existingSessionId != null) {
            this.removeSession(existingSessionId);
        } else {
            this.removeSession(sessionId);
        }
        this.setSession(sessionId, session);

    }

    @Override
    public void removeAndSetSession(String sessionId, HazelcastSession session) {
        this.removeAndSetSession(null, sessionId, session);
    }
}
