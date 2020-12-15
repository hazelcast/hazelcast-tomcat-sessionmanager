package com.hazelcast.session.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:clientServer.properties")
public class ClientServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClientServerApplication.class, args);
    }
}
