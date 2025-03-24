package com.hazelcast.session.sticky;

import com.hazelcast.session.Tomcat8Configurator;
import com.hazelcast.session.WebContainerConfigurator;
import com.hazelcast.test.HazelcastSerialClassRunner;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(HazelcastSerialClassRunner.class)
@Category(QuickTest.class)
public class Tomcat8P2PStickySessionsTest extends AbstractP2PStickySessionsTest {

    @Override
    protected WebContainerConfigurator<?> getWebContainerConfigurator() {
        return new Tomcat8Configurator();
    }
}
