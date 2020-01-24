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


import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;

import java.io.IOException;

public class ClientServerLifecycleListener implements LifecycleListener {

    private static ClientConfig config;
    private String configLocation;

    @Override
    public void lifecycleEvent(LifecycleEvent event) {

        if (getConfigLocation() == null) {
            setConfigLocation("hazelcast-client-default.xml");
        }

        if ("start".equals(event.getType())) {

            try {
                XmlClientConfigBuilder builder = new XmlClientConfigBuilder(getConfigLocation());
                config = builder.build();
            } catch (IOException e) {
                throw new RuntimeException("failed to load Config:", e);
            }

            if (config == null) {
                throw new RuntimeException("failed to find configLocation:" + getConfigLocation());
            }
        }
    }


    public String getConfigLocation() {
        return configLocation;
    }

    public void setConfigLocation(String configLocation) {
        this.configLocation = configLocation;
    }

    public static ClientConfig getConfig() {
        return config;
    }

}
