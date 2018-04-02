/*
 * Copyright (c) 2008-2016, Hazelcast, Inc. All Rights Reserved.
 */

package com.hazelcast.session;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.session.txsupport.MapQueryStrategy;
import com.hazelcast.session.txsupport.MapWriteStrategy;
import org.apache.catalina.Context;
import org.apache.catalina.Session;

import java.io.IOException;

public interface SessionManager {

    /**
     * Default name for the {@link com.hazelcast.core.HazelcastInstance}.
     */
    String DEFAULT_INSTANCE_NAME = "SESSION-REPLICATION-INSTANCE";

    void remove(Session session);

    Context getContext();

    void commit(Session session);

    String updateJvmRouteForSession(String sessionId, String newJvmRoute) throws IOException;

    String getJvmRoute();

    IMap<String, HazelcastSession> getDistributedMap();

    boolean isDeferredEnabled();

    HazelcastInstance getHazelcastInstance();

    MapQueryStrategy getMapQueryStrategy();

    MapWriteStrategy getMapWriteStrategy();
}
