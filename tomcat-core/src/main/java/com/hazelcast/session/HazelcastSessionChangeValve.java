/*
 * Copyright (c) 2008-2019, Hazelcast, Inc. All Rights Reserved.
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
