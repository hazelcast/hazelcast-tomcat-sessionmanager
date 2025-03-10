package com.hazelcast.session;

public class Tomcat11NonSerializableSessionTest
        extends AbstractNonSerializableSessionTest {
    @Override
    protected WebContainerConfigurator<?> getWebContainerConfigurator() {
        return new Tomcat11Configurator();
    }
}
