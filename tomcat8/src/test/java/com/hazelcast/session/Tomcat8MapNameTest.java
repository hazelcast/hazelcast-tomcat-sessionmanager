package com.hazelcast.session;

import org.junit.Rule;

public class Tomcat8MapNameTest extends AbstractMapNameTest{

    @Rule
    public Java6ExcludeRule java6ExcludeRule = new Java6ExcludeRule();

    @Override
    protected WebContainerConfigurator<?> getWebContainerConfigurator() {
        return new Tomcat8Configurator();
    }

}
