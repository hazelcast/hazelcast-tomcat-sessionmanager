package com.hazelcast.session;

public class Tomcat7NonSerializableSessionTest extends AbstractNonSerializableSessionTest {
    @Override
    protected WebContainerConfigurator<?> getWebContainerConfigurator() {
        return new Tomcat7Configurator();
    }
}
