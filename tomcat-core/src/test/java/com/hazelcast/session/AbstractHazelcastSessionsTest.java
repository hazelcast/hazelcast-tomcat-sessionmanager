package com.hazelcast.session;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.test.HazelcastTestSupport;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.After;

import java.net.ServerSocket;

public abstract class AbstractHazelcastSessionsTest extends HazelcastTestSupport {

    protected static int SERVER_PORT_1 = findFreeTCPPort();
    protected static int SERVER_PORT_2 = findFreeTCPPort();
    protected static String SESSION_REPLICATION_MAP_NAME = "session-replication-map";

    protected WebContainerConfigurator<?> instance1;
    protected WebContainerConfigurator<?> instance2;

    protected abstract WebContainerConfigurator<?> getWebContainerConfigurator();

    @After
    public void cleanup() throws Exception {
        instance1.stop();
        instance2.stop();
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
