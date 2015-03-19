package com.hazelcast.session;

public class Tomcat8MapNameTest extends AbstractMapNameTest{

    @Override
    protected WebContainerConfigurator<?> getWebContainerConfigurator() {
        return new Tomcat8Configurator();
    }

}
