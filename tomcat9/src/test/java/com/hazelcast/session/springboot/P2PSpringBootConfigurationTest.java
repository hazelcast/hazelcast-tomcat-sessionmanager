package com.hazelcast.session.springboot;

import org.junit.After;
import org.junit.Before;
import org.springframework.boot.SpringApplication;

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

}