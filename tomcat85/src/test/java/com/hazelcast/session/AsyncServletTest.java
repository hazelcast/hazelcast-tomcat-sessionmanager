package com.hazelcast.session;

public class AsyncServletTest extends AbstractAsyncServletTest {
    @Override
    protected WebContainerConfigurator<?> getAsyncWebContainerConfigurator() {
        return new Tomcat85AsyncConfigurator(temporaryFolder.getRoot().getAbsolutePath());
    }
}
