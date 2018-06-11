package com.hazelcast.session.nonsticky;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.session.AbstractHazelcastSessionsTest;
import com.hazelcast.session.HazelcastSession;
import org.apache.catalina.session.ManagerBase;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.junit.Test;

import static org.junit.Assert.*;

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

        HazelcastInstance instance = createHazelcastInstance();
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

    @Test
    public void givenSessionIsValidCheck_whenSessionShouldBeValid_thenEnsureSessionIsValid_andAccessTimesAreEqualOnBothNodes() throws Exception {

        CookieStore cookieStore = new BasicCookieStore();
        executeRequest("write", SERVER_PORT_1, cookieStore);
        String value = executeRequest("read", SERVER_PORT_1, cookieStore);
        assertEquals("value", value);

        String jSessionId = null;
        for (Cookie cookie : cookieStore.getCookies()) {
            if ("JSESSIONID".equalsIgnoreCase(cookie.getName())) {
                jSessionId = cookie.getValue();
            }
        }
        // Session timeout is 10
        sleepSeconds(9);

        executeRequest("write", SERVER_PORT_1, cookieStore);
        value = executeRequest("read", SERVER_PORT_1, cookieStore);
        assertEquals("value", value);

        sleepSeconds(5);

        HazelcastSession session1 = (HazelcastSession) ((ManagerBase) instance1.getManager()).findSession(jSessionId);
        HazelcastSession session2 = (HazelcastSession) ((ManagerBase) instance2.getManager()).findSession(jSessionId);

        assertNotNull("Session is present on Node 1", session1);
        assertNotNull("Session is present on Node 2", session2);

        assertTrue("Session is valid on Node 1", session1.isValid());
        assertTrue("Session is valid on Node 2", session2.isValid());
        validateSessionAccessTime(session1, session2);
    }

    public abstract void validateSessionAccessTime(HazelcastSession session1, HazelcastSession session2);
}
