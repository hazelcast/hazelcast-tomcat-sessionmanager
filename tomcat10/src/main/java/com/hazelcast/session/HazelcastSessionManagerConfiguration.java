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
import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import org.apache.catalina.Context;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Configuration
@ConditionalOnClass(HazelcastSessionManager.class)
@ConditionalOnProperty(name = "tsm.autoconfig.enabled", havingValue = "true", matchIfMissing = true)
public class HazelcastSessionManagerConfiguration {
    private static final String TSM_HAZELCAST_CONFIG_LOCATION = "tsm.config.location";
    private final Log log = LogFactory.getLog(HazelcastSessionManager.class);

    @Value("${" + TSM_HAZELCAST_CONFIG_LOCATION + ":hazelcast-default.xml}")
    private String configLocation;
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
    private boolean clientOnly;

    @Bean
    @ConditionalOnMissingBean(Config.class)
    @Conditional(HazelcastConfigAvailableCondition.class)
    public Config hazelcastConfigForTomcatSessionManager() throws Exception {
        Config config = new P2PConfigLoader().load(configLocation);
        if (config.getInstanceName() == null) {
            config.setInstanceName(SessionManager.DEFAULT_INSTANCE_NAME);
        }
        return config;
    }

    @Bean
    @ConditionalOnMissingBean(ClientConfig.class)
    @Conditional(HazelcastClientConfigAvailableCondition.class)
    public ClientConfig hazelcastClientConfigForTomcatSessionManager() throws Exception {
        clientOnly = true;
        ClientConfig clientConfig = new ClientServerConfigLoader().load(configLocation);
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

    private static ConditionOutcome getConditionOutcome(ConditionContext context, boolean forClient) {
        String message = "No explicit config provided using " + TSM_HAZELCAST_CONFIG_LOCATION;
        if (!context.getEnvironment().containsProperty(TSM_HAZELCAST_CONFIG_LOCATION)) {
            if (forClient) {
                return ConditionOutcome.noMatch(message);
            }
            return ConditionOutcome.match(message);
        }
        String configLocation = context.getEnvironment().getProperty(TSM_HAZELCAST_CONFIG_LOCATION);
        try {
            if (!forClient) {
                new P2PConfigLoader().load(configLocation);
            } else {
                new ClientServerConfigLoader().load(configLocation);
            }
            return ConditionOutcome.match("Found proper config at " + TSM_HAZELCAST_CONFIG_LOCATION);
        } catch (Exception e) {
            return ConditionOutcome.noMatch("No proper config at " + TSM_HAZELCAST_CONFIG_LOCATION
                    + ", Reason: " + e.getMessage());
        }
    }

    private static class HazelcastConfigAvailableCondition extends SpringBootCondition {
        @Override
        public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return getConditionOutcome(context, false);
        }
    }

    private static class HazelcastClientConfigAvailableCondition extends SpringBootCondition {
        @Override
        public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return getConditionOutcome(context, true);
        }
    }
}
