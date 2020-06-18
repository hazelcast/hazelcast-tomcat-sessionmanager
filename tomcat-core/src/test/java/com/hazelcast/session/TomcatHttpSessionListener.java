package com.hazelcast.session;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.HashSet;
import java.util.Set;

public class TomcatHttpSessionListener implements HttpSessionListener {

    Set<String> addedSessionSet = new HashSet<String>();
    Set<String> removedSessionSet = new HashSet<String>();

    @Override
    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        addedSessionSet.add(httpSessionEvent.getSession().getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        removedSessionSet.add(httpSessionEvent.getSession().getId());
    }

    public Set<String> getAddedSessions() {
        return addedSessionSet;
    }

    public Set<String> getRemovedSessions() {
        return removedSessionSet;
    }
}
