package com.hazelcast.session.sticky;

import com.hazelcast.enterprise.EnterpriseSerialJUnitClassRunner;
import com.hazelcast.session.Tomcat6Configurator;
import com.hazelcast.session.WebContainerConfigurator;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(EnterpriseSerialJUnitClassRunner.class)
@Category(QuickTest.class)
public class Tomcat6ClientServerStickySessionsTest extends AbstractClientServerStickySessionsTest {

    @Override
    protected WebContainerConfigurator<?> getWebContainerConfigurator() {
        return new Tomcat6Configurator();
    }
}
