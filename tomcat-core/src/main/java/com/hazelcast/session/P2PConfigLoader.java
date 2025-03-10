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
import com.hazelcast.config.ConfigRecognizer;
import com.hazelcast.config.ConfigStream;
import com.hazelcast.config.InvalidConfigurationException;
import com.hazelcast.config.UrlXmlConfig;
import com.hazelcast.config.UrlYamlConfig;
import com.hazelcast.internal.config.MemberXmlConfigRootTagRecognizer;
import com.hazelcast.internal.config.MemberYamlConfigRootTagRecognizer;

import java.net.URL;

import static com.hazelcast.internal.config.ConfigLoader.locateConfig;

class P2PConfigLoader {
    private final ConfigRecognizer xmlConfigRecognizer;
    private final ConfigRecognizer yamlConfigRecognizer;

    P2PConfigLoader() throws Exception {
        xmlConfigRecognizer = new MemberXmlConfigRootTagRecognizer();
        yamlConfigRecognizer = new MemberYamlConfigRootTagRecognizer();
    }

    Config load(final String path) throws Exception {
        final URL url = locateConfig(path);
        if (url == null) {
            return null;
        }
        ConfigStream xmlConfigStream = new ConfigStream(url.openStream());
        try {
            if (xmlConfigRecognizer.isRecognized(xmlConfigStream)) {
                return new UrlXmlConfig(url);
            }
        } finally {
            xmlConfigStream.close();
        }
        ConfigStream yamlConfigStream = new ConfigStream(url.openStream());
        try {
            if (yamlConfigRecognizer.isRecognized(yamlConfigStream)) {
                return new UrlYamlConfig(url);
            }
        } finally {
            yamlConfigStream.close();
        }
        throw new InvalidConfigurationException("The provided file is not a valid Hazelcast member configuration: " + url);
    }
}
