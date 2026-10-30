package com.hazelcast.session;

import com.hazelcast.test.HazelcastSerialClassRunner;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(HazelcastSerialClassRunner.class)
@Category(QuickTest.class)
public class Tomcat9SessionExpireTest
        extends AbstractSessionExpireTest {
    @Override
    protected WebContainerConfigurator<?> getWebContainerConfigurator() {
        return new Tomcat9Configurator();
    }
}
