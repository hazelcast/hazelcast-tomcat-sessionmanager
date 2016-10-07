package com.hazelcast.session;

public class Tomcat6NonSerializableSessionTest extends AbstractNonSerializableSessionTest {
    @Override
    protected WebContainerConfigurator<?> getWebContainerConfigurator() {
        return new Tomcat6Configurator();
    }
}
