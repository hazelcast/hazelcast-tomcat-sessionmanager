package com.hazelcast.session;

import com.hazelcast.test.HazelcastSerialClassRunner;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(HazelcastSerialClassRunner.class)
@Category(QuickTest.class)
public class Tomcat10SecurityTest
        extends AbstractTomcatSecurityTest {
    @Override
    protected WebContainerConfigurator<?> getTomcatConfigurator(String appName) {
        return new Tomcat10Configurator(appName);
    }
}
