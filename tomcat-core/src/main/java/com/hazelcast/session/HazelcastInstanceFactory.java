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

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.apache.catalina.LifecycleException;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

/**
 * Factory for {@link HazelcastInstance}'s for session management
 */
public final class HazelcastInstanceFactory {
    private static final Log LOGGER = LogFactory.getLog(HazelcastInstanceFactory.class);

    private HazelcastInstanceFactory() {
    }

    /**
     * Gets a {@link HazelcastInstance} by creating a new one (or getting the existing one on P2P setup).
     *
     * @param classLoader the classloader to set for the {@link HazelcastInstance}
     * @param clientOnly states if the instance is a client or not
     * @param instanceName name of the Hazelcast instance
     * @return the Hazelcast instance created/found
     * @throws LifecycleException when {@link HazelcastInstance} cannot be created in client/server mode
     */
    public static HazelcastInstance getHazelcastInstance(ClassLoader classLoader, boolean clientOnly, String instanceName)
            throws LifecycleException {
        HazelcastInstance instance;
        if (instanceName != null) {
            if (clientOnly) {
                instance = HazelcastClient.getHazelcastClientByName(instanceName);
            } else {
                instance = Hazelcast.getHazelcastInstanceByName(instanceName);
            }
        } else if (clientOnly) {
            try {
                ClientConfig clientConfig = ClientServerLifecycleListener.getConfig();
                clientConfig.setClassLoader(classLoader);
                instance = HazelcastClient.newHazelcastClient(clientConfig);
            } catch (Exception e) {
                LOGGER.error("Hazelcast Client could not be created.", e);
                throw new LifecycleException(e.getMessage());
            }
        } else {
            /*
             Note that Hazelcast instance can only set a classloader during the initialization. If the context classloader
             changes after the initialization of the Hazelcast instance, it cannot be changed for the Hazelcast instance.
             */
            Config config = P2PLifecycleListener.getConfig();
            config.setClassLoader(classLoader);
            instance = Hazelcast.getOrCreateHazelcastInstance(config);
        }

        return instance;
    }
}
