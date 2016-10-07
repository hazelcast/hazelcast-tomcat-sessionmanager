package com.hazelcast.session;

import com.hazelcast.core.Hazelcast;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public abstract class AbstractNonSerializableSessionTest extends AbstractHazelcastSessionsTest {
    @Test
    public void testSerialization() throws Exception {
        Hazelcast.newHazelcastInstance();

        instance1 = getWebContainerConfigurator();
        instance1.port(SERVER_PORT_1).sticky(true).clientOnly(true).mapName(SESSION_REPLICATION_MAP_NAME).sessionTimeout(10).start();
        instance2 = getWebContainerConfigurator();
        instance2.port(SERVER_PORT_2).sticky(true).clientOnly(true).mapName(SESSION_REPLICATION_MAP_NAME).sessionTimeout(10).start();

        CookieStore cookieStore = new BasicCookieStore();
        assertEquals("true", executeRequest("nonserializable", SERVER_PORT_1, cookieStore));
    }
}
