package com.hazelcast.session;

import org.apache.catalina.Session;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import javax.servlet.ServletException;
import java.io.IOException;


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
