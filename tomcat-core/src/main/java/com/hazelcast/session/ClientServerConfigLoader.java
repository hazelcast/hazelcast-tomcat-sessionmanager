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
import com.hazelcast.client.config.YamlClientConfigBuilder;
import com.hazelcast.client.config.impl.ClientXmlConfigRootTagRecognizer;
import com.hazelcast.client.config.impl.ClientYamlConfigRootTagRecognizer;
import com.hazelcast.config.ConfigRecognizer;
import com.hazelcast.config.ConfigStream;
import com.hazelcast.config.InvalidConfigurationException;

import java.net.URL;

import static com.hazelcast.internal.config.ConfigLoader.locateConfig;

class ClientServerConfigLoader {
    private final ConfigRecognizer xmlConfigRecognizer;
    private final ConfigRecognizer yamlConfigRecognizer;

    ClientServerConfigLoader() throws Exception {
        xmlConfigRecognizer = new ClientXmlConfigRootTagRecognizer();
        yamlConfigRecognizer = new ClientYamlConfigRootTagRecognizer();
    }

    ClientConfig load(final String path) throws Exception {
        final URL url = locateConfig(path);
        if (url == null) {
            return null;
        }
        ConfigStream xmlConfigStream = new ConfigStream(url.openStream());
        try {
            if (xmlConfigRecognizer.isRecognized(xmlConfigStream)) {
                return new XmlClientConfigBuilder(url).build();
            }
        } finally {
            xmlConfigStream.close();
        }
        ConfigStream yamlConfigStream = new ConfigStream(url.openStream());
        try {
            if (yamlConfigRecognizer.isRecognized(yamlConfigStream)) {
                return new YamlClientConfigBuilder(url).build();
            }
        } finally {
            yamlConfigStream.close();
        }
        throw new InvalidConfigurationException("The provided file is not a valid Hazelcast client configuration: " + url);
    }
}
