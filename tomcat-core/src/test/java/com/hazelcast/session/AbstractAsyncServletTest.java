package com.hazelcast.session;

import com.hazelcast.core.Hazelcast;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;

public abstract class AbstractAsyncServletTest extends AbstractHazelcastSessionsTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        Hazelcast.newHazelcastInstance();

        instance1 = getWebContainerConfigurator();
        instance1.port(SERVER_PORT_1).sticky(true).clientOnly(true).sessionTimeout(10).start();
        instance2 = getWebContainerConfigurator();
        instance2.port(SERVER_PORT_2).sticky(true).clientOnly(true).sessionTimeout(10).start();
    }

    @Override
    protected WebContainerConfigurator<?> getWebContainerConfigurator() {
        return getAsyncWebContainerConfigurator();
    }

    protected abstract WebContainerConfigurator<?> getAsyncWebContainerConfigurator();

    @Test
    public void testReadWriteRead() throws Exception {
        CookieStore cookieStore = new BasicCookieStore();
        String value = executeRequest("", SERVER_PORT_1, cookieStore);
        assertEquals(value, "OK");
    }
}
