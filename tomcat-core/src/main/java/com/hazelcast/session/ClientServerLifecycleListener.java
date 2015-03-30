package com.hazelcast.session;


import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.instance.GroupProperties;
import com.hazelcast.license.util.LicenseHelper;
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
            String licenseKey = config.getProperty(GroupProperties.PROP_ENTERPRISE_LICENSE_KEY);
            LicenseHelper.checkLicenseKey(licenseKey);

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
