package com.hazelcast.session.springboot;

import org.junit.After;
import org.junit.Before;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

public class P2PSpringBootConfigurationTest
        extends AbstractSpringBootConfigurationTest {

    @Before
    public void setup() {
        applicationContext = SpringApplication.run(P2PApplication.class);
    }

    @After
    public void clean() {
        SpringApplication.exit(applicationContext);
    }

    @SpringBootApplication
    @ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
            classes = ClientServerSpringBootConfigurationTest.ClientServerApplication.class))
    static class P2PApplication {
        public static void main(String[] args) {
            SpringApplication.run(P2PApplication.class, args);
        }
    }
}