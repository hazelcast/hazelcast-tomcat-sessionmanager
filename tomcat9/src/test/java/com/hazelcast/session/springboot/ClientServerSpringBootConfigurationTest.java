package com.hazelcast.session.springboot;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.junit.After;
import org.junit.Before;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

public class ClientServerSpringBootConfigurationTest
        extends AbstractSpringBootConfigurationTest {

    private HazelcastInstance hazelcastInstance;

    @Before
    public void setup() {
        hazelcastInstance = Hazelcast.newHazelcastInstance();
        applicationContext = SpringApplication.run(ClientServerApplication.class);
    }

    @After
    public void clean() {
        SpringApplication.exit(applicationContext);
        hazelcastInstance.shutdown();
    }

    @SpringBootApplication
    @PropertySource("classpath:clientServer.properties")
    static class ClientServerApplication {
        public static void main(String[] args) {
            SpringApplication.run(ClientServerApplication.class, args);
        }
    }
}