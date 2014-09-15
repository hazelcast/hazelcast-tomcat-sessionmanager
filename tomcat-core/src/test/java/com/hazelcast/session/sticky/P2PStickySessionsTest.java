package com.hazelcast.session.sticky;

import org.junit.Before;

/**
 * Created by mesutcelik on 6/12/14.
 */
public abstract class P2PStickySessionsTest extends AbstractStickySessionsTest {

    @Before
    public void init() throws Exception {
        instance1 = getWebContainerConfigurator();
        instance1.port(SERVER_PORT_1).sticky(true).clientOnly(false).sessionTimeout(10).start();
        instance2 = getWebContainerConfigurator();
        instance2.port(SERVER_PORT_2).sticky(true).clientOnly(false).sessionTimeout(10).start();
    }

}
