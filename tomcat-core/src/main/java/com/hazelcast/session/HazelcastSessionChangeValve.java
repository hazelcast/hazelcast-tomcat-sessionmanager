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

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

import javax.servlet.ServletException;
import java.io.IOException;

public class HazelcastSessionChangeValve extends ValveBase {

    private SessionManager sessionManager;

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
            requestedJvmRoute = currentSessionId.substring(index + 1, currentSessionId.length());
        }

        if (requestedJvmRoute == null || requestedJvmRoute.equals(jvmRoute)) {
            return;
        }

        String newSessionId = sessionManager.updateJvmRouteForSession(currentSessionId, jvmRoute);
        request.changeSessionId(newSessionId);
    }
}
