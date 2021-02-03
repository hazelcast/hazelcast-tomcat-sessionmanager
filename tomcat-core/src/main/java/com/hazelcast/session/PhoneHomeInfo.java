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

import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 * Creates query string according to plugin properties to be sent to phone home
 * server by {@link PhoneHomeService}.
 */
class PhoneHomeInfo {

    private static final String PROPERTIES_RESOURCE = "/phone.home.properties";

    private final String version;
    private final String queryString;

    PhoneHomeInfo(String tomcatVersion, boolean clientOnly, boolean sticky, boolean deferredWrite, boolean instanceNameDefault) {
        this.version = resolveVersion();
        this.queryString = buildQueryString(tomcatVersion, clientOnly, sticky, deferredWrite, instanceNameDefault);
    }

    String getQueryString() {
        return queryString;
    }

    static String resolveVersion() {
        Properties properties = new Properties();
        try {
            InputStream propertiesStream = PhoneHomeInfo.class.getResourceAsStream(PROPERTIES_RESOURCE);
            properties.load(propertiesStream);
            return properties.getProperty("project.version", "N/A");
        } catch (IOException ignored) {
            return "N/A";
        }
    }

    private String buildQueryString(String tomcatVersion, boolean clientOnly, boolean sticky, boolean deferredWrite,
                                    boolean instanceNameDefault) {
        // Any change committed here must correspond to the phone
        // home server changes. Do not make standalone changes
        // especially for the parameter keys.
        return new QueryStringBuilder()
                .addParam("version", version)
                .addParam("tomcat-version", tomcatVersion)
                .addParam("client-only", String.valueOf(clientOnly))
                .addParam("sticky", String.valueOf(sticky))
                .addParam("deferred-write", String.valueOf(deferredWrite))
                .addParam("instance-name-default", String.valueOf(instanceNameDefault))
                .build();
    }

    private static class QueryStringBuilder {

        private final ILogger logger = Logger.getLogger(QueryStringBuilder.class);
        private final StringBuilder builder = new StringBuilder("?");

        private QueryStringBuilder addParam(String key, String value) {
            if (builder.length() > 1) {
                builder.append("&");
            }
            builder.append(key).append("=").append(tryEncode(value));
            return this;
        }

        private String tryEncode(String value) {
            try {
                return URLEncoder.encode(value, Charset.forName("UTF-8").toString());
            } catch (UnsupportedEncodingException e) {
                if (logger.isFineEnabled()) {
                    logger.fine("Using <unknown> for the value which couldn't be encoded: " + value, e);
                }
                // return the known encoding of the word `unknown` which is unchanged.
                return "unknown";
            }
        }

        private String build() {
            return builder.toString();
        }
    }

}
