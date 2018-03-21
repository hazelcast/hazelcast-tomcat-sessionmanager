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
        final int SESSION_TIMEOUT_IN_MINUTES = 1;
        final int EXTRA_DELAY_IN_SECONDS = 5;

        instance1 = getWebContainerConfigurator();
        instance1.port(SERVER_PORT_1).sticky(true).clientOnly(false).mapName(SESSION_REPLICATION_MAP_NAME).sessionTimeout(SESSION_TIMEOUT_IN_MINUTES).start();
        instance2 = getWebContainerConfigurator();
        instance2.port(SERVER_PORT_2).sticky(true).clientOnly(false).mapName(SESSION_REPLICATION_MAP_NAME).sessionTimeout(SESSION_TIMEOUT_IN_MINUTES).start();

        CookieStore cookieStore = new BasicCookieStore();
        executeRequest("write", SERVER_PORT_1, cookieStore);
        String value = executeRequest("read", SERVER_PORT_1, cookieStore);
        assertEquals("value", value);

        instance1.stop();

        HazelcastInstance hzInstance1 = Hazelcast.getHazelcastInstanceByName("hzInstance1");
        if (hzInstance1 != null) {
            hzInstance1.shutdown();
        }


        sleepSeconds(SESSION_TIMEOUT_IN_MINUTES * 60 + EXTRA_DELAY_IN_SECONDS);

        assertEquals(0, instance2.getManager().getDistributedMap().size());
    }
}
