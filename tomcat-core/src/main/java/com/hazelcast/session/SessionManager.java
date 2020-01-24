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

import com.hazelcast.map.IMap;
import org.apache.catalina.Session;

import java.io.IOException;

public interface SessionManager {

    /**
     * Default name for the {@link com.hazelcast.core.HazelcastInstance}.
     */
    String DEFAULT_INSTANCE_NAME = "SESSION-REPLICATION-INSTANCE";

    void remove(Session session);

    void commit(Session session);

    String updateJvmRouteForSession(String sessionId, String newJvmRoute) throws IOException;

    String getJvmRoute();

    IMap<String, HazelcastSession> getDistributedMap();

    boolean isDeferredEnabled();

    /**
     * @return true if this {@link SessionManager} has sticky sessions enabled, otherwise false.
     */
    boolean isSticky();
}
