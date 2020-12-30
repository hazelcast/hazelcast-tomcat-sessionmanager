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

package com.hazelcast.session.springboot;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.session.ClientServerConfigLoader;
import com.hazelcast.session.HazelcastSessionManager;
import com.hazelcast.session.P2PConfigLoader;
import com.hazelcast.session.SessionManager;
import org.apache.catalina.Context;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
@ConditionalOnClass(HazelcastSessionManager.class)
public class HazelcastSessionManagerConfiguration {
    private final Log log = LogFactory.getLog(HazelcastSessionManager.class);

    @Value("${tsm.hazelcast.config.location:hazelcast-default.xml}")
    private String configLocation;
    @Value("${tsm.hazelcast.client.config.location:hazelcast-client-default.xml}")
    private String clientConfigLocation;
    @Value("${tsm.client.only:false}")
    private boolean clientOnly;
    @Value("${tsm.map.name:default}")
    private String mapName;
    @Value("${tsm.sticky:true}")
    private boolean sticky;
    @Value("${tsm.process.expires.frequency:6}")
    private int processExpiresFrequency;
    @Value("${tsm.deferred.write:true}")
    private boolean deferredWrite;
    @Value("${tsm.hazelcast.instance.name:" + SessionManager.DEFAULT_INSTANCE_NAME + "}")
    private String hazelcastInstanceName;

    @Bean
    @ConditionalOnMissingBean(type = "com.hazelcast.config.Config")
    @ConditionalOnProperty(name = "tsm.client.only", havingValue = "false", matchIfMissing = true)
    public Config hazelcastConfig() throws Exception {
        Config config = new P2PConfigLoader().load(configLocation);
        if (config.getInstanceName() == null) {
            config.setInstanceName(SessionManager.DEFAULT_INSTANCE_NAME);
        }
        return config;
    }

    @Bean
    @ConditionalOnMissingBean(type = "com.hazelcast.client.config.ClientConfig")
    @ConditionalOnProperty(name = "tsm.client.only", havingValue = "true")
    public ClientConfig hazelcastClientConfig() throws Exception {
        ClientConfig clientConfig = new ClientServerConfigLoader().load(clientConfigLocation);
        if (clientConfig.getInstanceName() == null) {
            clientConfig.setInstanceName(SessionManager.DEFAULT_INSTANCE_NAME);
        }
        return clientConfig;
    }

    @Bean(name = "hazelcastTomcatSessionManagerCustomizer")
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> customizeTomcat(HazelcastInstance hazelcastInstance) {
        return new WebServerFactoryCustomizer<TomcatServletWebServerFactory>() {
            @Override
            public void customize(TomcatServletWebServerFactory factory) {
                factory.addContextCustomizers(new TomcatContextCustomizer() {
                    @Override
                    public void customize(Context context) {
                        HazelcastSessionManager manager = new HazelcastSessionManager();
                        manager.setClientOnly(clientOnly);
                        manager.setMapName(mapName);
                        manager.setSticky(sticky);
                        manager.setProcessExpiresFrequency(processExpiresFrequency);
                        manager.setDeferredWrite(deferredWrite);
                        manager.setHazelcastInstanceName(hazelcastInstanceName);
                        context.setManager(manager);
                        log.info(String.format(
                                "Tomcat context is configured with HazelcastSessionManager => clientOnly: %s, mapName: %s, "
                                        + "isSticky: %s, processExpiresFrequency: %d, deferredWrite: %s, "
                                        + "hazelcastInstanceName: %s",
                                clientOnly, mapName, sticky, processExpiresFrequency, deferredWrite, hazelcastInstanceName));
                    }
                });
            }
        };
    }
}
