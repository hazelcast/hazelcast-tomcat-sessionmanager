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

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HazelcastSessionChangeValve extends ValveBase {

    private final Log log = LogFactory.getLog(HazelcastSessionChangeValve.class);

    private final SessionManager sessionManager;

    private final Map<String, String> handledSessions = new HashMap<String, String>();

    public HazelcastSessionChangeValve(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        handleTomcatSessionChange(request);
        getNext().invoke(request, response);
    }

    private void handleTomcatSessionChange(Request request) throws IOException {
        String currentSessionId = request.getRequestedSessionId();
        if (currentSessionId == null) {
            return;
        }
        String jvmRoute = sessionManager.getJvmRoute();
        int index = currentSessionId.indexOf(".");
        String requestedJvmRoute = null;
        if (index > 0) {
            requestedJvmRoute = currentSessionId.substring(index + 1);
        }

        if (requestedJvmRoute == null || requestedJvmRoute.equals(jvmRoute)) {
            return;
        }

        request.changeSessionId(getOrCreateHandledSessionId(currentSessionId, jvmRoute));
        request.getSession().invalidate();
    }

    private String getOrCreateHandledSessionId(String currentSessionId, String jvmRoute)
            throws IOException {
        log.debug(String.format("Thread name: %s, Handling session id: %s", Thread.currentThread().getName(), currentSessionId));
        String handledSessionId;
        synchronized (this) {
            if (!handledSessions.containsKey(currentSessionId)) {
                handledSessions.put(currentSessionId, sessionManager.updateJvmRouteForSession(currentSessionId, jvmRoute));
            }
        }
        handledSessionId = handledSessions.get(currentSessionId);

        log.info(String.format("Thread name: %s, Handled session id from %s to %s",
                Thread.currentThread().getName(), currentSessionId, handledSessionId));
        return handledSessionId;
    }
}
