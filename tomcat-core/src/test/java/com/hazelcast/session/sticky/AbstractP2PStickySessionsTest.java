package com.hazelcast.session.sticky;

import org.junit.Before;

public abstract class AbstractP2PStickySessionsTest extends AbstractStickySessionsTest {

    @Before
    public void init() throws Exception {
        instance1 = getWebContainerConfigurator();
        instance1.port(SERVER_PORT_1)
                .sticky(true)
                .clientOnly(false)
                .sessionTimeout(10)
                .configLocation("hazelcast-1.xml")
                .start();

        instance2 = getWebContainerConfigurator();
        instance2.port(SERVER_PORT_2)
                .sticky(true)
                .clientOnly(false)
                .sessionTimeout(10)
                .configLocation("hazelcast-2.xml")
                .start();
    }
}
