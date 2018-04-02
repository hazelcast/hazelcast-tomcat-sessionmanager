package com.hazelcast.session.nonsticky;

import org.junit.Before;

public abstract class AbstractTwoPhaseCommitP2PNonStickySessionsTest extends AbstractNonStickySessionsTest {

    @Before
    public void init() throws Exception {
        instance1 = getWebContainerConfigurator();
        instance1.port(SERVER_PORT_1).sticky(false).clientOnly(false).writeStrategy("twoPhaseCommit").sessionTimeout(10).start();
        instance2 = getWebContainerConfigurator();
        instance2.port(SERVER_PORT_2).sticky(false).clientOnly(false).writeStrategy("twoPhaseCommit").sessionTimeout(10).start();
    }
}
