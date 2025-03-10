package com.hazelcast.session;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Hazelcast;
import org.apache.catalina.Manager;
import org.apache.catalina.session.StandardSession;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.After;

import java.io.IOException;
import java.net.ServerSocket;

public abstract class AbstractHazelcastSessionsTest {

    protected static int SERVER_PORT_1 = findFreeTCPPort();
    protected static int SERVER_PORT_2 = findFreeTCPPort();
    protected static String SESSION_REPLICATION_MAP_NAME = "session-replication-map";

    protected WebContainerConfigurator<?> instance1;
    protected WebContainerConfigurator<?> instance2;

    protected abstract WebContainerConfigurator<?> getWebContainerConfigurator();

    @After
    public void cleanup() throws Exception {
        if (instance1 != null) {
            instance1.stop();
        }
        if (instance2 != null) {
            instance2.stop();
        }
        HazelcastClient.shutdownAll();
        Hazelcast.shutdownAll();
    }

    protected String executeRequest(String context, int serverPort, CookieStore cookieStore) throws Exception {
        HttpClient client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
        HttpGet request = new HttpGet("http://localhost:" + serverPort + "/" + context);
        HttpResponse response = client.execute(request);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity);
    }

    /**
     * Helper method to retrieve the JSESSIONID value from the {@link CookieStore}.
     * @param cookieStore the cookie store containing sessions.
     * @return the value of the JSESSIONID cookie if present, otherwise null.
     */
    protected static String getJSessionId(CookieStore cookieStore) {
        String jSessionId = null;
        for (Cookie cookie : cookieStore.getCookies()) {
            if ("JSESSIONID".equalsIgnoreCase(cookie.getName())) {
                jSessionId = cookie.getValue();
            }
        }
        return jSessionId;
    }

    /**
     * Retrieves sessions using {@link Manager#findSession(String)} in accordance with the {@link StandardSession#isValid()}
     * method.
     *
     * @param jSessionId the session id.
     * @param instance the tomcat instance.
     * @return the instance of {@link HazelcastSession} if present, otherwise null.
     */
    protected static HazelcastSession getHazelcastSession(String jSessionId, WebContainerConfigurator<?> instance)
            throws IOException {
        return (HazelcastSession) ((Manager) instance.getManager()).findSession(jSessionId);
    }

    /**
     * Returns any free local TCP port number available.
     */
    private static int findFreeTCPPort() {
        try {
            ServerSocket serverSocket = new ServerSocket(0);
            int localPort = serverSocket.getLocalPort();
            serverSocket.close();
            return localPort;
        } catch (Exception e) {
            throw new IllegalStateException("Could not find any available port", e);
        }
    }
}
