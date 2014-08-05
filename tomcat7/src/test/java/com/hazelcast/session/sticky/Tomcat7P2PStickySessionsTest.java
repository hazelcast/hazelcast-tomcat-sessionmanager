package com.hazelcast.session.sticky;

import com.hazelcast.session.Tomcat7Configurator;
import com.hazelcast.session.WebContainerConfigurator;

/**
 * Created by mesutcelik on 6/12/14.
 */
public class Tomcat7P2PStickySessionsTest extends P2PStickySessionsTest {

    @Override
    protected WebContainerConfigurator<?> getWebContainerConfigurator() {
        return new Tomcat7Configurator();
    }
}
