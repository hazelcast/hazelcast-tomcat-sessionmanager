package com.hazelcast.session;

public class Tomcat9NonSerializableSessionTest
        extends AbstractNonSerializableSessionTest {
    @Override
    protected WebContainerConfigurator<?> getWebContainerConfigurator() {
        return new Tomcat9Configurator();
    }
}
