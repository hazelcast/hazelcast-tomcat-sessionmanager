package com.hazelcast.session;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.test.HazelcastSerialClassRunner;
import com.hazelcast.test.annotation.QuickTest;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by mesutcelik on 5/5/14.
 */
@RunWith(HazelcastSerialClassRunner.class)
@Category(QuickTest.class)
public abstract class AbstractMapNameTest extends AbstractHazelcastSessionsTest {

    @Test
    public void testMapName() throws Exception{

        HazelcastInstance hz = Hazelcast.newHazelcastInstance();

        instance1 = getWebContainerConfigurator();
        instance1.port(SERVER_PORT_1).sticky(true).clientOnly(true).mapName(SESSION_REPLICATION_MAP_NAME).sessionTimeout(10).start();
        instance2 = getWebContainerConfigurator();
        instance2.port(SERVER_PORT_2).sticky(true).clientOnly(true).mapName(SESSION_REPLICATION_MAP_NAME).sessionTimeout(10).start();

        CookieStore cookieStore = new BasicCookieStore();
        executeRequest("write", SERVER_PORT_1, cookieStore);

        Cookie cookie = cookieStore.getCookies().get(0);
        String sessionId = cookie.getValue();

        IMap<String,HazelcastSession> map = hz.getMap(SESSION_REPLICATION_MAP_NAME);
        assertEquals(1,map.size());
        HazelcastSession session = map.get(sessionId);

        assertFalse(session.getAttributes().isEmpty());

        executeRequest("remove", SERVER_PORT_1, cookieStore);
        cookie = cookieStore.getCookies().get(0);
        String newSessionId = cookie.getValue();
        session = map.get(newSessionId);

        assertTrue(session.getAttributes().isEmpty());

    }


}
