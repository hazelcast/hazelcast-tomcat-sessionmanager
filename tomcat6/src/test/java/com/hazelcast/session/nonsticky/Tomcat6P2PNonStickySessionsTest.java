package com.hazelcast.session.nonsticky;

import com.hazelcast.session.Tomcat6Configurator;
import com.hazelcast.session.WebContainerConfigurator;

/**
 * Created by mesutcelik on 6/12/14.
 */
public class Tomcat6P2PNonStickySessionsTest extends P2PNonStickySessionsTest {

    @Override
    protected WebContainerConfigurator<?> getWebContainerConfigurator() {
        return new Tomcat6Configurator();
    }
}
