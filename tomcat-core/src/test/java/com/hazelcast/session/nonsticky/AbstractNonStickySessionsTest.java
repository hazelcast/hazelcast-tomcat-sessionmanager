package com.hazelcast.session.nonsticky;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.session.AbstractHazelcastSessionsTest;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public abstract class AbstractNonStickySessionsTest extends AbstractHazelcastSessionsTest {

    @Test
    public void testContextReloadNonSticky() throws Exception {
        CookieStore cookieStore = new BasicCookieStore();
        executeRequest("write", SERVER_PORT_1, cookieStore);
        instance1.reload();

        String value = executeRequest("read", SERVER_PORT_2, cookieStore);
        assertEquals("value", value);
    }

    @Test
    public void testReadWriteRead() throws Exception {
        CookieStore cookieStore = new BasicCookieStore();
        String value = executeRequest("read", SERVER_PORT_1, cookieStore);
        assertEquals("null", value);

        executeRequest("write", SERVER_PORT_1, cookieStore);

        value = executeRequest("read", SERVER_PORT_2, cookieStore);
        assertEquals("value", value);
    }

    @Test(timeout = 60000)
    public void testAttributeDistribution() throws Exception {
        CookieStore cookieStore = new BasicCookieStore();
        executeRequest("write", SERVER_PORT_1, cookieStore);

        String value = executeRequest("read", SERVER_PORT_2, cookieStore);
        assertEquals("value", value);
    }

    @Test(timeout = 60000)
    public void testAttributeRemoval() throws Exception {
        CookieStore cookieStore = new BasicCookieStore();
        executeRequest("write", SERVER_PORT_1, cookieStore);

        String value = executeRequest("read", SERVER_PORT_2, cookieStore);
        assertEquals("value", value);

        value = executeRequest("remove", SERVER_PORT_2, cookieStore);
        assertEquals("true", value);

        value = executeRequest("read", SERVER_PORT_1, cookieStore);
        assertEquals("null", value);
    }

    @Test(timeout = 60000)
    public void testAttributeUpdate() throws Exception {
        CookieStore cookieStore = new BasicCookieStore();
        executeRequest("write", SERVER_PORT_1, cookieStore);

        String value = executeRequest("read", SERVER_PORT_2, cookieStore);
        assertEquals("value", value);

        value = executeRequest("update", SERVER_PORT_2, cookieStore);
        assertEquals("true", value);

        value = executeRequest("read", SERVER_PORT_1, cookieStore);
        assertEquals("value-updated", value);
    }

    @Test(timeout = 60000)
    public void testAttributeInvalidate() throws Exception {
        CookieStore cookieStore = new BasicCookieStore();
        executeRequest("write", SERVER_PORT_1, cookieStore);

        String value = executeRequest("read", SERVER_PORT_2, cookieStore);
        assertEquals("value", value);

        value = executeRequest("invalidate", SERVER_PORT_2, cookieStore);
        assertEquals("true", value);

        HazelcastInstance instance = HazelcastClient.newHazelcastClient();
        IMap<Object, Object> map = instance.getMap("default");
        assertEquals(0, map.size());
    }

    @Test(timeout = 60000)
    public void testAttributeNames() throws Exception {
        CookieStore cookieStore = new BasicCookieStore();
        executeRequest("read", SERVER_PORT_1, cookieStore);

        String commaSeparatedAttributeNames = executeRequest("names", SERVER_PORT_2, cookieStore);

        // no name should be created
        assertEquals("", commaSeparatedAttributeNames);

        executeRequest("write", SERVER_PORT_2, cookieStore);

        commaSeparatedAttributeNames = executeRequest("names", SERVER_PORT_1, cookieStore);
        assertEquals("key", commaSeparatedAttributeNames);

    }

    @Test(timeout = 60000)
    public void test_isNew() throws Exception {
        CookieStore cookieStore = new BasicCookieStore();

        assertEquals("true", executeRequest("isNew", SERVER_PORT_1, cookieStore));
        assertEquals("false", executeRequest("isNew", SERVER_PORT_2, cookieStore));
    }

    @Test(timeout = 60000)
    public void test_LastAccessTime() throws Exception {
        CookieStore cookieStore = new BasicCookieStore();
        String lastAccessTime1 = executeRequest("lastAccessTime", SERVER_PORT_1, cookieStore);
        executeRequest("lastAccessTime", SERVER_PORT_2, cookieStore);
        String lastAccessTime2 = executeRequest("lastAccessTime", SERVER_PORT_2, cookieStore);

        assertNotEquals(lastAccessTime1, lastAccessTime2);
    }
}
