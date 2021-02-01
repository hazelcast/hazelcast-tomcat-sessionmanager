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

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.hazelcast.internal.nio.IOUtil.closeResource;
import static java.lang.Boolean.FALSE;
import static java.lang.System.getenv;

/**
 * Pings phone home server with plugin information daily.
 */
class PhoneHomeService {

    private static final String SYS_PHONE_HOME_ENABLED = "hazelcast.phone.home.enabled";
    private static final String ENV_PHONE_HOME_ENABLED = "HZ_PHONE_HOME_ENABLED";

    private static final int TIMEOUT_IN_MS = 3000;
    private static final int RETRY_COUNT = 5;
    private static final boolean PHONE_HOME_ENABLED = isPhoneHomeEnabled();
    private static final AtomicBoolean STARTED = new AtomicBoolean();
    private static ScheduledThreadPoolExecutor executor;

    private final ILogger logger = Logger.getLogger(PhoneHomeService.class);

    private final String baseUrl;
    private final PhoneHomeInfo phoneHomeInfo;
    private ScheduledFuture<?> sendFuture;

    static {
        if (PHONE_HOME_ENABLED) {
            executor = new ScheduledThreadPoolExecutor(0, new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "Hazelcast-TomcatSessionManager.PhoneHomeService");
                    t.setDaemon(true);
                    return t;
                }
            });
        }
    }

    PhoneHomeService(PhoneHomeInfo phoneHomeInfo) {
        this("http://phonehome.hazelcast.com/pingIntegrations/hazelcast-tomcat-sessionmanager", phoneHomeInfo);
    }

    PhoneHomeService(String baseUrl, PhoneHomeInfo phoneHomeInfo) {
        this.baseUrl = baseUrl;
        this.phoneHomeInfo = phoneHomeInfo;
    }

    String getBaseUrl() {
        return baseUrl;
    }

    private static boolean isPhoneHomeEnabled() {
        if (FALSE.toString().equalsIgnoreCase(System.getProperty(SYS_PHONE_HOME_ENABLED))) {
            return false;
        }
        return !FALSE.toString().equalsIgnoreCase(getenv(ENV_PHONE_HOME_ENABLED));
    }

    void start() {
        if (PHONE_HOME_ENABLED && STARTED.compareAndSet(false, true)) {
            sendFuture = executor.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    send();
                }
            }, 0, 1, TimeUnit.DAYS);
        }
    }

    boolean isStarted() {
        return STARTED.get();
    }

    private void send() {
        int retryCount = RETRY_COUNT;
        boolean succeed = false;
        while (retryCount-- > 0 && !succeed) {
            InputStream in = null;
            try {
                URL url = new URL(baseUrl + phoneHomeInfo.getQueryString());
                URLConnection conn = url.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.setConnectTimeout(TIMEOUT_IN_MS);
                conn.setReadTimeout(TIMEOUT_IN_MS);
                in = new BufferedInputStream(conn.getInputStream());
                succeed = true;
            } catch (Exception e) {
                if (logger.isFineEnabled()) {
                    logger.fine("Failed to establish home phone call. Retries left: " + retryCount, e);
                }
            } finally {
                closeResource(in);
            }
        }
    }

    void shutdown() {
        if (PHONE_HOME_ENABLED && STARTED.compareAndSet(true, false)) {
            if (sendFuture != null) {
                sendFuture.cancel(false);
            }
        }
    }
}
