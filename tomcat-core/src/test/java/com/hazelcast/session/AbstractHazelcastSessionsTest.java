package com.hazelcast.session;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.session.txsupport.DefaultMapWriteStrategy;
import com.hazelcast.session.txsupport.OnePhaseCommitMapWriteStrategy;
import com.hazelcast.session.txsupport.TwoPhaseCommitMapWriteStrategy;
import com.hazelcast.test.HazelcastTestSupport;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Test;

import java.net.ServerSocket;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

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

    public String getWriteStrategy() {
        return "default";
    }

    @Test
    public void givenWriteStrategyIsSet_thenVerifyCorrectImplementation() {
        assertEquals(getWriteStrategy(), ((AbstractHazelcastSessionManager) instance1.getManager()).getWriteStrategy());
        assertEquals(getWriteStrategy(), ((AbstractHazelcastSessionManager) instance2.getManager()).getWriteStrategy());

        if ("default".equals(getWriteStrategy())) {
            assertTrue(instance1.getManager().getMapWriteStrategy() instanceof DefaultMapWriteStrategy);
            assertTrue(instance2.getManager().getMapWriteStrategy() instanceof DefaultMapWriteStrategy);
        } else if ("onePhaseCommit".equals(getWriteStrategy())) {
            assertTrue(instance1.getManager().getMapWriteStrategy() instanceof OnePhaseCommitMapWriteStrategy);
            assertTrue(instance2.getManager().getMapWriteStrategy() instanceof OnePhaseCommitMapWriteStrategy);
        } else if ("twoPhaseCommit".equals(getWriteStrategy())) {
            assertTrue(instance1.getManager().getMapWriteStrategy() instanceof TwoPhaseCommitMapWriteStrategy);
            assertTrue(instance2.getManager().getMapWriteStrategy() instanceof TwoPhaseCommitMapWriteStrategy);
        } else {
            assertTrue(instance1.getManager().getMapWriteStrategy() instanceof DefaultMapWriteStrategy);
            assertTrue(instance2.getManager().getMapWriteStrategy() instanceof DefaultMapWriteStrategy);
        }
    }
}
