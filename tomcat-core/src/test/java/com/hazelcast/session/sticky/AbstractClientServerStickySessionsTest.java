package com.hazelcast.session.sticky;

import com.hazelcast.core.Hazelcast;
import org.junit.Before;

public abstract class AbstractClientServerStickySessionsTest extends AbstractStickySessionsTest {

    @Before
    public void init() throws Exception{
        Hazelcast.newHazelcastInstance();
        instance1 = getWebContainerConfigurator();
        instance1.port(SERVER_PORT_1).sticky(true).clientOnly(true).sessionTimeout(10).start();
        instance2 = getWebContainerConfigurator();
        instance2.port(SERVER_PORT_2).sticky(true).clientOnly(true).sessionTimeout(10).start();
    }
}
