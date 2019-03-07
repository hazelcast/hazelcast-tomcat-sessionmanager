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
public class HazelcastInstanceFactory {
    private static final Log log = LogFactory.getLog(HazelcastInstanceFactory.class);

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
        if (clientOnly) {
            try {
                ClientConfig clientConfig = ClientServerLifecycleListener.getConfig();
                clientConfig.setClassLoader(classLoader);
                instance = HazelcastClient.newHazelcastClient(clientConfig);
            } catch (Exception e) {
                log.error("Hazelcast Client could not be created.", e);
                throw new LifecycleException(e.getMessage());
            }
        } else if (instanceName != null) {
            instance = Hazelcast.getHazelcastInstanceByName(instanceName);
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
