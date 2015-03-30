package com.hazelcast.session.license;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.session.AbstractHazelcastSessionsTest;
import com.hazelcast.session.Tomcat8Configurator;
import com.hazelcast.session.WebContainerConfigurator;
import org.junit.After;
import org.junit.Test;

public class Tomcat8ValidLicenseCheck extends AbstractHazelcastSessionsTest {

    @Override
    protected WebContainerConfigurator<?> getWebContainerConfigurator() {
        return new Tomcat8Configurator("hazelcast-with-valid-license.xml","hazelcast-client-with-valid-license.xml");
    }

    @After
    @Override
    public void cleanup() throws Exception {
        Hazelcast.shutdownAll();
        instance1.stop();
    }

    @Test
    public void testClientServerWithValidLicense() throws Exception{
        Hazelcast.newHazelcastInstance();
        instance1 = getWebContainerConfigurator();
        instance1.port(SERVER_PORT_1).sticky(false).clientOnly(true).sessionTimeout(10).start();
    }

    @Test
    public void testP2PWithValidLicense() throws Exception{
        instance1 = getWebContainerConfigurator();
        instance1.port(SERVER_PORT_1).sticky(false).clientOnly(false).sessionTimeout(10).start();
    }
}

