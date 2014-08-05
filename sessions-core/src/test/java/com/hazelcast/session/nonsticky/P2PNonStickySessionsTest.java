package com.hazelcast.session.nonsticky;

import org.junit.Before;

/**
 * Created by mesutcelik on 6/12/14.
 */
public abstract class P2PNonStickySessionsTest extends AbstractNonStickySessionsTest {

    @Before
    public void init() throws Exception {
        instance1 = getWebContainerConfigurator();
        instance1.port(SERVER_PORT_1).sticky(false).clientOnly(false).sessionTimeout(10).start();
        instance2 = getWebContainerConfigurator();
        instance2.port(SERVER_PORT_2).sticky(false).clientOnly(false).sessionTimeout(10).start();
    }

}
