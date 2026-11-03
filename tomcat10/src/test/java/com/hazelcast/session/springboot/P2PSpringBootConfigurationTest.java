package com.hazelcast.session.springboot;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.session.HazelcastSessionManagerConfiguration;
import org.junit.After;
import org.junit.Before;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

public class P2PSpringBootConfigurationTest
        extends AbstractSpringBootConfigurationTest {

    private HazelcastInstance hazelcastInstance;

    @Before
    public void setup() {
        hazelcastInstance = Hazelcast.newHazelcastInstance();
        applicationContext = SpringApplication.run(SpringP2PApplication.class);
    }

    @After
    public void clean() {
        SpringApplication.exit(applicationContext);
        hazelcastInstance.shutdown();
    }

    @SpringBootApplication
    @PropertySource("classpath:clientServer.properties")
    static class SpringP2PApplication {
        public static void main(String[] args) {
            SpringApplication.run(SpringP2PApplication.class, args);
        }
    }
}