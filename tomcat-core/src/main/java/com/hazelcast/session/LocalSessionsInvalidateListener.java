package com.hazelcast.session;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.listener.EntryEvictedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import org.apache.catalina.Session;

import java.util.Map;

public class LocalSessionsInvalidateListener implements EntryEvictedListener<String, HazelcastSession>,
        EntryRemovedListener<String, HazelcastSession> {

    private Map<String, Session> sessions;

    public LocalSessionsInvalidateListener(Map<String, Session> sessions) {
        this.sessions = sessions;
    }

    @Override
    public void entryEvicted(EntryEvent<String, HazelcastSession> event) {
        invalidateSessions(event);
    }

    @Override
    public void entryRemoved(EntryEvent<String, HazelcastSession> event) {
        invalidateSessions(event);
    }

    private void invalidateSessions(EntryEvent<String, HazelcastSession> entryEvent) {
        if (entryEvent.getMember() == null || !entryEvent.getMember().localMember()) {
            sessions.remove(entryEvent.getKey());
        }
    }
}
