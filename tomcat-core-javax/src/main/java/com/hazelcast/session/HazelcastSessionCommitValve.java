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

import org.apache.catalina.Session;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import java.io.IOException;

import javax.servlet.ServletException;

public class HazelcastSessionCommitValve extends ValveBase {

    private final Log log = LogFactory.getLog(HazelcastSessionCommitValve.class);

    private SessionManager sessionManager;

    public HazelcastSessionCommitValve(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        try {
            getNext().invoke(request, response);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            final Session session = request.getSessionInternal(false);
            storeOrRemoveSession(session);
        }
    }

    private void storeOrRemoveSession(Session session) {
        if (session != null) {
            if (session.isValid()) {
                log.trace("Request with session completed, saving session " + session.getId());
                if (session.getSession() != null) {
                    log.trace("HTTP Session present, saving " + session.getId());
                    sessionManager.commit(session);
                } else {
                    log.trace("No HTTP Session present, Not saving " + session.getId());
                }
            } else {
                log.trace("HTTP Session has been invalidated, removing :" + session.getId());
                sessionManager.remove(session);
            }
        }
    }
}
