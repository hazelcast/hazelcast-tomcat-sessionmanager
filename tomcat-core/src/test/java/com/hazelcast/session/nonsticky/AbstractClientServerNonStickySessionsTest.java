package com.hazelcast.session.nonsticky;

import com.hazelcast.core.Hazelcast;
import org.junit.Before;

public abstract class AbstractClientServerNonStickySessionsTest extends AbstractNonStickySessionsTest {

    @Before
    public void init() throws Exception {
        Hazelcast.newHazelcastInstance();
        instance1 = getWebContainerConfigurator();
        instance1.port(SERVER_PORT_1).sticky(false).clientOnly(true).writeStrategy(getWriteStrategy()).sessionTimeout(10).start();
        instance2 = getWebContainerConfigurator();
        instance2.port(SERVER_PORT_2).sticky(false).clientOnly(true).writeStrategy(getWriteStrategy()).sessionTimeout(10).start();
    }
}
