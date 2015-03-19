package com.hazelcast.session.sticky;

import com.hazelcast.session.Java6ExcludeRule;
import com.hazelcast.session.Tomcat8Configurator;
import com.hazelcast.session.WebContainerConfigurator;
import org.junit.Rule;

public class Tomcat8P2PStickySessionsTest extends P2PStickySessionsTest {

    @Rule
    public Java6ExcludeRule java6ExcludeRule = new Java6ExcludeRule();

    @Override
    protected WebContainerConfigurator<?> getWebContainerConfigurator() {
        return new Tomcat8Configurator();
    }
}
