package com.hazelcast.session.springboot;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.session.HazelcastSessionManager;
import org.apache.catalina.Context;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.tomcat.TomcatWebServer;
import org.springframework.boot.web.server.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class P2PSpringBootConfigurationTest
        extends AbstractSpringBootConfigurationTest {

    @Before
    public void setup() {
        applicationContext = SpringApplication.run(SpringP2PApplication.class,
                "--tsm.config.location=classpath:hazelcast-p2p.xml");
    }

    @After
    public void clean() {
        if (applicationContext != null) {
            ((ConfigurableApplicationContext) applicationContext).close();
        }
    }

    @Test
    public void testMemberMode() {
        HazelcastInstance instance = applicationContext.getBean(HazelcastInstance.class);
        assertSame(instance, Hazelcast.getHazelcastInstanceByName(instance.getName()));
        assertTrue(HazelcastClient.getAllHazelcastClients().isEmpty());

        ServletWebServerApplicationContext webContext = (ServletWebServerApplicationContext) applicationContext;
        TomcatWebServer webServer = (TomcatWebServer) webContext.getWebServer();
        Context context = (Context) webServer.getTomcat().getHost().findChild("");
        HazelcastSessionManager manager = (HazelcastSessionManager) context.getManager();
        assertFalse(manager.isClientOnly());
    }

    @SpringBootApplication
    @ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
            classes = ClientServerSpringBootConfigurationTest.ClientServerApplication.class))
    static class SpringP2PApplication {
        public static void main(String[] args) {
            SpringApplication.run(SpringP2PApplication.class, args);
        }
    }
}
