package com.hazelcast.session.txsupport;

import com.hazelcast.core.IMap;
import com.hazelcast.session.HazelcastSession;

public class DefaultMapQueryStrategy implements MapQueryStrategy {
    private final IMap<String, HazelcastSession> sessionMap;

    public DefaultMapQueryStrategy(IMap<String, HazelcastSession> sessionMap) {
        this.sessionMap = sessionMap;
    }

    @Override
    public HazelcastSession getSession(String sessionId) {
        return sessionMap.get(sessionId);
    }

}
