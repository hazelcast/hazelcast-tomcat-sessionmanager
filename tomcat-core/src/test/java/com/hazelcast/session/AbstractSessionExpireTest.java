package com.hazelcast.session;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class AbstractSessionExpireTest extends AbstractHazelcastSessionsTest {

    TomcatHttpSessionListener listener1;
    TomcatHttpSessionListener listener2;

    @Test
    public void testSessionExpiration() throws Exception {
        final int SESSION_TIMEOUT_IN_MINUTES = 1;
        final int SESSION_TIMEOUT_IN_SECONDS = SESSION_TIMEOUT_IN_MINUTES * 60;
        final int EXTRA_DELAY_IN_SECONDS = 5;

        initializeInstances("hazelcast-1.xml", "hazelcast-2.xml", SESSION_TIMEOUT_IN_MINUTES);

        executeCall(SERVER_PORT_1);

        sleepSeconds(SESSION_TIMEOUT_IN_SECONDS / 2);

        executeCall(SERVER_PORT_2);

        validateNumberOfSessionsAccordingToSessionListener(2);
        validateNumberOfSessionsAccordingToSessionMap(2, instance1);

        sleepSeconds(SESSION_TIMEOUT_IN_SECONDS / 2 + EXTRA_DELAY_IN_SECONDS);

        validateNumberOfSessionsAccordingToSessionListener(1);
        validateNumberOfSessionsAccordingToSessionMap(1, instance1);

        sleepSeconds(SESSION_TIMEOUT_IN_SECONDS / 2 + EXTRA_DELAY_IN_SECONDS);

        validateNumberOfSessionsAccordingToSessionListener(0);
        validateNumberOfSessionsAccordingToSessionMap(0, instance1);

        instance1.stop();
        instance2.stop();
    }

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

        executeCall(SERVER_PORT_1);

        validateNumberOfSessionsAccordingToSessionListener(1);
        validateNumberOfSessionsAccordingToSessionMap(1, instance2);

        instance1.stop();

        shutdownHzInstance1();

        sleepSeconds(SESSION_TIMEOUT_IN_MINUTES * 60 + EXTRA_DELAY_IN_SECONDS);

        validateNumberOfSessionsAccordingToSessionListener(expectedSessionCount);
        validateNumberOfSessionsAccordingToSessionMap(expectedSessionCount, instance2);

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

        validateNumberOfSessionsAccordingToSessionListener(0);
        validateNumberOfSessionsAccordingToSessionMap(0, instance2);

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

        listener1 = getHttpSessionListener(instance1);
        listener2 = getHttpSessionListener(instance2);
        assertNotNull(listener1);
        assertNotNull(listener2);

    }

    private void executeCall(int serverPort) throws Exception {
        CookieStore cookieStore = new BasicCookieStore();
        executeRequest("write", serverPort, cookieStore);
        String value = executeRequest("read", serverPort, cookieStore);
        assertEquals("value", value);
    }

    private void validateNumberOfSessionsAccordingToSessionMap(int expectedSessionCount, WebContainerConfigurator<?> instance) {
        assertEquals(expectedSessionCount, instance.getManager().getDistributedMap().size());
    }

    private void validateNumberOfSessionsAccordingToSessionListener(int expectedSessionCount) {
        Set<String> sessions = new HashSet<String>();
        sessions.addAll(listener1.getAddedSessions());
        sessions.addAll(listener2.getAddedSessions());
        sessions.removeAll(listener1.getRemovedSessions());
        sessions.removeAll(listener2.getRemovedSessions());

        assertEquals(expectedSessionCount, sessions.size());
    }

    private TomcatHttpSessionListener getHttpSessionListener(WebContainerConfigurator<?> instance) {
        Object[] eventListeners = instance.getContext().getApplicationLifecycleListeners();

        for (Object eventListener : eventListeners) {
            if (eventListener instanceof TomcatHttpSessionListener) {
                return (TomcatHttpSessionListener) eventListener;
            }
        }

        return null;
    }
}
