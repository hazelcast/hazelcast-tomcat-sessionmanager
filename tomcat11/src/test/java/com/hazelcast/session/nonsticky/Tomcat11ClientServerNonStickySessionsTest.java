package com.hazelcast.session.nonsticky;

import com.hazelcast.session.HazelcastSession;
import com.hazelcast.session.Tomcat11Configurator;
import com.hazelcast.session.WebContainerConfigurator;
import com.hazelcast.test.HazelcastSerialClassRunner;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(HazelcastSerialClassRunner.class)
@Category(QuickTest.class)
public class Tomcat11ClientServerNonStickySessionsTest
        extends AbstractClientServerNonStickySessionsTest {

    @Override
    protected WebContainerConfigurator<?> getWebContainerConfigurator() {
        return new Tomcat11Configurator();
    }

    @Override
    public void validateSessionAccessTime(HazelcastSession session1, HazelcastSession session2) {
        assertEquals("Session lastAccessTime should be equal", session1.getLastAccessedTime(), session2.getLastAccessedTime());
        assertEquals("Session thisAccessedTime should be equal", session1.getThisAccessedTime(), session2.getThisAccessedTime());
    }
}
