package com.hazelcast.session;

public class Tomcat8NonSerializableSessionTest extends AbstractNonSerializableSessionTest {
    @Override
    protected WebContainerConfigurator<?> getWebContainerConfigurator() {
        return new Tomcat8Configurator();
    }
}
