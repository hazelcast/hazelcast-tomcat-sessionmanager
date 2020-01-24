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
