package com.hazelcast.session;

/**
 * Created by mesutcelik on 6/4/14.
 */
public class Tomcat7MapNameTest extends AbstractMapNameTest{

    @Override
    protected WebContainerConfigurator<?> getWebContainerConfigurator() {
        return new Tomcat7Configurator();
    }

}
