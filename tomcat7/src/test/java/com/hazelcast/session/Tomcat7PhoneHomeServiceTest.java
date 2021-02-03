package com.hazelcast.session;

import com.hazelcast.test.HazelcastSerialClassRunner;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

@RunWith(HazelcastSerialClassRunner.class)
@Category(QuickTest.class)
public class Tomcat7PhoneHomeServiceTest
        extends AbstractPhoneHomeServiceTest {
    @Override
    protected WebContainerConfigurator<?> getWebContainerConfigurator() {
        return new Tomcat7Configurator();
    }

    @Test
    public void phoneHomeServiceInitTest() {
        //when
        HazelcastSessionManager sessionManager = new HazelcastSessionManager();

        //then
        assertNotNull(sessionManager.getPhoneHomeService());
    }
}
