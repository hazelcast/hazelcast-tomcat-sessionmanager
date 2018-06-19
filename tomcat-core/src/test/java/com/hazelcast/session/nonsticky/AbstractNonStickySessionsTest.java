package com.hazelcast.session.nonsticky;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.session.AbstractHazelcastSessionsTest;
import com.hazelcast.session.HazelcastSession;
import com.hazelcast.session.WebContainerConfigurator;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.session.StandardSession;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
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
    public void givenValidSession_whenNonStickySessions_thenAccessTimesAreEqualOnAllNodes() throws Exception {
        CookieStore cookieStore = new BasicCookieStore();
        assertEquals("true", executeRequest("isNew", SERVER_PORT_1, cookieStore));
        String jSessionId = getJSessionId(cookieStore);

        HazelcastSession session1 = getHazelcastSession(jSessionId, instance1);
        HazelcastSession session2 = getHazelcastSession(jSessionId, instance2);

        validateSessionAccessTime(session1, session2);
    }

    public abstract void validateSessionAccessTime(HazelcastSession session1, HazelcastSession session2);


    /**
     * Helper method to retrieve the JSESSIONID value from the {@link CookieStore}.
     * @param cookieStore the cookie store containing sessions.
     * @return the value of the JSESSIONID cookie if present, otherwise null.
     */
    private static String getJSessionId(CookieStore cookieStore) {
        String jSessionId = null;
        for (Cookie cookie : cookieStore.getCookies()) {
            if ("JSESSIONID".equalsIgnoreCase(cookie.getName())) {
                jSessionId = cookie.getValue();
            }
        }
        return jSessionId;
    }

    /**
     * Retrieves sessions using {@link Manager#findSessions()} in accordance with the {@link StandardSession#isValid()}
     * method.
     *
     * @param jSessionId the session id.
     * @param instance the tomcat instance.
     * @return the instance of {@link HazelcastSession} if present, otherwise null.
     */
    private static HazelcastSession getHazelcastSession(String jSessionId, WebContainerConfigurator<?> instance) {
        Session[] allSessions = ((Manager) instance.getManager()).findSessions();

        HazelcastSession hzSession = null;
        for (Session session : allSessions) {
            if (jSessionId.equals(session.getId())) {
                hzSession = (HazelcastSession) session;
                break;
            }
        }
        return hzSession;
    }
}
