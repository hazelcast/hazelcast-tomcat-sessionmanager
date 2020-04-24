package com.hazelcast.session;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public abstract class AbstractSessionExpireTest extends AbstractHazelcastSessionsTest {

    @Test
    public void testSessionExpireAfterFailoverAndSessionTimeout() throws Exception {
        testSessionExpireAfterFailover("hazelcast-1.xml", "hazelcast-2.xml", 0);
    }

    @Test
    public void testSessionExpireAfterFailoverAndSessionTimeout_withDifferentHzExpirationConfiguration() throws Exception {
        testSessionExpireAfterFailover("hazelcast-3.xml", "hazelcast-4.xml", 1);
    }

    private void testSessionExpireAfterFailover(String firstConfig, String secondConfig, int expectedSessionCount)
            throws Exception {
        final int SESSION_TIMEOUT_IN_MINUTES = 1;
        final int EXTRA_DELAY_IN_SECONDS = 5;

        initializeInstances(firstConfig, secondConfig, SESSION_TIMEOUT_IN_MINUTES);

        CookieStore cookieStore = new BasicCookieStore();
        executeRequest("write", SERVER_PORT_1, cookieStore);
        String value = executeRequest("read", SERVER_PORT_1, cookieStore);
        assertEquals("value", value);

        instance1.stop();

        shutdownHzInstance1();

        sleepSeconds(SESSION_TIMEOUT_IN_MINUTES * 60 + EXTRA_DELAY_IN_SECONDS);

        assertEquals(expectedSessionCount, instance2.getManager().getDistributedMap().size());

        instance2.stop();
    }

    @Test
    public void testSessionExpireAfterFailoverAndSessionTimeout_withSessionSpecificTimeout()
            throws Exception {
        final int GENERIC_SESSION_TIMEOUT_IN_MINUTES = 10;
        final int SPECIFIC_SESSION_TIMEOUT_IN_MINUTES = 1;
        final int EXTRA_DELAY_IN_SECONDS = 5;

        initializeInstances("hazelcast-1.xml", "hazelcast-2.xml", GENERIC_SESSION_TIMEOUT_IN_MINUTES);

        CookieStore cookieStore = new BasicCookieStore();
        executeRequest("write", SERVER_PORT_1, cookieStore);

        String sessionId = getJSessionId(cookieStore);
        HazelcastSession session = getHazelcastSession(sessionId, instance1);
        session.setMaxInactiveInterval(SPECIFIC_SESSION_TIMEOUT_IN_MINUTES);

        String value = executeRequest("read", SERVER_PORT_1, cookieStore);
        assertEquals("value", value);

        instance1.stop();

        shutdownHzInstance1();

        sleepSeconds(SPECIFIC_SESSION_TIMEOUT_IN_MINUTES * 60 + EXTRA_DELAY_IN_SECONDS);

        assertEquals(0, instance2.getManager().getDistributedMap().size());

        instance2.stop();
    }

    private void shutdownHzInstance1() {
        HazelcastInstance hzInstance1 = Hazelcast.getHazelcastInstanceByName("hzInstance1");
        if (hzInstance1 != null) {
            hzInstance1.shutdown();
        }
    }

    private void initializeInstances(String firstConfig, String secondConfig, int sessionTimeout)
            throws Exception {
        instance1 = getWebContainerConfigurator();
        instance1.port(SERVER_PORT_1).sticky(true).clientOnly(false).mapName(SESSION_REPLICATION_MAP_NAME)
                 .sessionTimeout(sessionTimeout).configLocation(firstConfig).start();

        instance2 = getWebContainerConfigurator();
        instance2.port(SERVER_PORT_2).sticky(true).clientOnly(false).mapName(SESSION_REPLICATION_MAP_NAME)
                 .sessionTimeout(sessionTimeout).configLocation(secondConfig).start();
    }
}
