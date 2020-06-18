package com.hazelcast.session;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.listener.EntryExpiredListener;
import org.apache.catalina.Manager;

public class ExpireEvictedSessionsListener implements EntryExpiredListener<String, HazelcastSession> {

    private Manager manager;

    public ExpireEvictedSessionsListener(Manager manager) {
        this.manager = manager;
    }

    @Override
    public void entryExpired(EntryEvent<String, HazelcastSession> event) {
        HazelcastSession session = event.getOldValue();
        if (session.isValid()) {
            session.setManager(manager);
            session.expire();
        }
    }

}
