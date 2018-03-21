package com.hazelcast.session;

public class Tomcat85SessionExpireTest extends AbstractSessionExpireTest {
    @Override
    protected WebContainerConfigurator<?> getWebContainerConfigurator() {
        return new Tomcat85Configurator();
    }
}
