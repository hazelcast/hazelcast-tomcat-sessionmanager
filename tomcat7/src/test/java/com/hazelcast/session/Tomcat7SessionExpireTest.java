package com.hazelcast.session;

public class Tomcat7SessionExpireTest extends AbstractSessionExpireTest {
    @Override
    protected WebContainerConfigurator<?> getWebContainerConfigurator() {
        return new Tomcat7Configurator();
    }
}
