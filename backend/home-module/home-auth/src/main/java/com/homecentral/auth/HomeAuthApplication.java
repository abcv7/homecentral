package com.homecentral.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.homecentral")
@SpringBootApplication
public class HomeAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(HomeAuthApplication.class, args);
    }
}
