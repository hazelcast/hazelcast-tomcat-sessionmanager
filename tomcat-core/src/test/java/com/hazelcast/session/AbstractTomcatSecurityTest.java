package com.hazelcast.session;

import com.hazelcast.core.Hazelcast;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public abstract class AbstractTomcatSecurityTest extends AbstractHazelcastSessionsTest {
    @Override
    protected WebContainerConfigurator<?> getWebContainerConfigurator() {
        return getTomcatConfigurator("appWithSecurity");
    }

    @Before
    public void init() throws Exception {
        Hazelcast.newHazelcastInstance();
        instance1 = getWebContainerConfigurator();
        instance1.port(SERVER_PORT_1).sticky(false).clientOnly(true).writeStrategy(getWriteStrategy()).sessionTimeout(10).start();
        instance2 = getWebContainerConfigurator();
        instance2.port(SERVER_PORT_2).sticky(false).clientOnly(true).writeStrategy(getWriteStrategy()).sessionTimeout(10).start();
    }

    @Test
    public void testGetProtectedResourceFormLogin() throws Exception {
        CookieStore cookieStore = new BasicCookieStore();

        assertTrue(executeRequest("secureEndpoint", SERVER_PORT_1, cookieStore).contains("redirected to LoginServlet"));
    }

    protected abstract WebContainerConfigurator<?> getTomcatConfigurator(String appName);
}
