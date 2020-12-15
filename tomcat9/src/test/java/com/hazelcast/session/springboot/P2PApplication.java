package com.hazelcast.session.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = ClientServerApplication.class))
public class P2PApplication {
    public static void main(String[] args) {
        SpringApplication.run(P2PApplication.class, args);
    }
}
