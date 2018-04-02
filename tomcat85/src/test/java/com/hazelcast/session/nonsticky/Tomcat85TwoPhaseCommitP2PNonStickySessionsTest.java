package com.hazelcast.session.nonsticky;

import com.hazelcast.session.Tomcat85Configurator;
import com.hazelcast.session.WebContainerConfigurator;
import com.hazelcast.test.HazelcastSerialClassRunner;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(HazelcastSerialClassRunner.class)
@Category(QuickTest.class)
public class Tomcat85TwoPhaseCommitP2PNonStickySessionsTest extends AbstractTwoPhaseCommitP2PNonStickySessionsTest{

    @Override
    protected WebContainerConfigurator<?> getWebContainerConfigurator() {
        return new Tomcat85Configurator();
    }
}
