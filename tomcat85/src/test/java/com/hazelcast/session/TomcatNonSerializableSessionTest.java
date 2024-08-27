package com.hazelcast.session;

public class TomcatNonSerializableSessionTest extends AbstractNonSerializableSessionTest {
    @Override
    protected WebContainerConfigurator<?> getWebContainerConfigurator() {
        return new TomcatConfigurator();
    }
}
