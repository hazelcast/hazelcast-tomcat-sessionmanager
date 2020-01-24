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

import com.hazelcast.config.Config;
import com.hazelcast.internal.config.ConfigLoader;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;

import java.io.IOException;

public class P2PLifecycleListener implements LifecycleListener {
    private static Config config;
    private String configLocation;

    @Override
    public void lifecycleEvent(LifecycleEvent event) {
        String shutdown = System.getProperty("hazelcast.tomcat.shutdown_hazelcast_instance");
        if (getConfigLocation() == null) {
            setConfigLocation("hazelcast-default.xml");
        }

        if ("start".equals(event.getType())) {
            try {
                config = ConfigLoader.load(getConfigLocation());
            } catch (IOException e) {
                throw new RuntimeException("failed to load Config:", e);
            }

            if (config == null) {
                throw new RuntimeException("failed to find configLocation:" + getConfigLocation());
            }
            if (config.getInstanceName() == null) {
                config.setInstanceName(SessionManager.DEFAULT_INSTANCE_NAME);
            }
        } else if ("stop".equals(event.getType()) && !"false".equals(shutdown)) {
            HazelcastInstance instance = Hazelcast.getHazelcastInstanceByName(SessionManager.DEFAULT_INSTANCE_NAME);
            if (instance != null) {
                instance.shutdown();
            }
        }
    }


    public String getConfigLocation() {
        return configLocation;
    }

    public void setConfigLocation(String configLocation) {
        this.configLocation = configLocation;
    }

    public static Config getConfig() {
        return config;
    }
}
