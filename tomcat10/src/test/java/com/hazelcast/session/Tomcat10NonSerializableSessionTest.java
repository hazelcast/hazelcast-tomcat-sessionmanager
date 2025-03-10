package com.hazelcast.session;

public class Tomcat10NonSerializableSessionTest
        extends AbstractNonSerializableSessionTest {
    @Override
    protected WebContainerConfigurator<?> getWebContainerConfigurator() {
        return new Tomcat10Configurator();
    }
}
