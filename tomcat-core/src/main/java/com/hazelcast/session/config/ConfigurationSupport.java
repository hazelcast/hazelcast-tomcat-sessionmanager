package com.hazelcast.session.config;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.session.ClientServerLifecycleListener;
import com.hazelcast.session.P2PLifecycleListener;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

/**
 * Utility class containing common configuration support methods.
 */
public class ConfigurationSupport {

    private static final Log log = LogFactory.getLog(ConfigurationSupport.class);

    private ConfigurationSupport() { /* Intentionally Empty to prevent instantiation . */ }

    /**
     * Factory method to resolve the hazelcast tomcat session map name from the manager settings for the context.
     *
     * @param context           the catalina context.
     * @param configuredMapName the name of the hazelcast map configured on the context manager element.
     * @return the resolved session map name.
     */
    public static String resolveSessionMapName(Context context, final String configuredMapName) {
        final String mapName;
        if (configuredMapName == null || "default".equals(configuredMapName)) {
            String contextPath = context.getServletContext().getContextPath();
            log.info("contextPath: " + contextPath);
            if (contextPath == null || contextPath.equals("/") || contextPath.equals("")) {
                mapName = "empty_session_replication";
            } else {
                mapName = contextPath.substring(1, contextPath.length()) + "_session_replication";
            }
        } else {
            mapName = configuredMapName;
        }
        log.info("sessionMapName: " + mapName);
        return mapName;
    }

    /**
     * Factory method to resolve the {@link HazelcastInstance} used for the {@code HazelcastSessionManager} based on
     * the Manager configuration settings for the context.
     * If {@code isClientOnly} is true, a new Hazelcast Client instance will be created.
     * If {@code hazelcastInstanceName} is not null, a new hazelcast instance will be created by name.
     * Otherwise, a new P2P Hazecast Instance will be created.
     *
     * @param isClientOnly          create a new Hazelcast client instance.
     * @param context               the catalina context.
     * @param hazelcastInstanceName the name of the hazelcast instance if it is to be instantiated by name.
     * @return the configured Hazelcast session instance.
     */
    public static HazelcastInstance getOrCreateHazelcastInstance(boolean isClientOnly, Context context, String hazelcastInstanceName) throws LifecycleException {
        final HazelcastInstance instance;
        if (isClientOnly) {
            try {
                ClientConfig clientConfig = ClientServerLifecycleListener.getConfig();
                clientConfig.setClassLoader(context.getLoader().getClassLoader());
                instance = HazelcastClient.newHazelcastClient(clientConfig);
            } catch (Exception e) {
                log.error("Hazelcast Client could not be created.", e);
                throw new LifecycleException(e.getMessage());
            }
        } else if (hazelcastInstanceName != null) {
            instance = Hazelcast.getHazelcastInstanceByName(hazelcastInstanceName);
        } else {
            instance = Hazelcast.getOrCreateHazelcastInstance(P2PLifecycleListener.getConfig());
        }

        return instance;
    }
}
