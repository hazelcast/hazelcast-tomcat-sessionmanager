/*
 * Copyright (c) 2008-2016, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.session;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.instance.BuildInfo;
import com.hazelcast.instance.BuildInfoProvider;
import com.hazelcast.instance.GroupProperty;
import com.hazelcast.license.domain.LicenseType;
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
            String licenseKey = config.getLicenseKey();
            if (licenseKey == null) {
                licenseKey = config.getProperty(GroupProperty.ENTERPRISE_LICENSE_KEY.getName());
            }
            final BuildInfo buildInfo = BuildInfoProvider.getBuildInfo();
            LicenseHelper.checkLicenseKey(licenseKey, buildInfo.getVersion(), LicenseType.ENTERPRISE, LicenseType.ENTERPRISE_HD);
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
