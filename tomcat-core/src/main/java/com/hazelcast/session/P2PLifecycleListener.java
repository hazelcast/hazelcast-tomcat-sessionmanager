package com.hazelcast.session;

import com.hazelcast.config.Config;
import com.hazelcast.config.ConfigLoader;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.license.util.LicenseHelper;
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
            setConfigLocation("hazelcast.xml");
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
            config.setInstanceName(SessionManager.DEFAULT_INSTANCE_NAME);
            Hazelcast.getOrCreateHazelcastInstance(config);
            String licenseKey = config.getLicenseKey();
            LicenseHelper.checkLicenseKey(licenseKey);

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
