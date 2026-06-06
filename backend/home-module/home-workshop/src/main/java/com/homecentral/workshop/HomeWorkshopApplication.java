package com.homecentral.workshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableFeignClients(basePackages = {
        "com.homecentral.friend.api.feign",
        "com.homecentral.notification.api.feign",
        "com.homecentral.auth.api.feign"
})
@EnableScheduling
@SpringBootApplication(scanBasePackages = {
        "com.homecentral.workshop",
        "com.homecentral.common"
})
public class HomeWorkshopApplication {

    public static void main(String[] args) {
        SpringApplication.run(HomeWorkshopApplication.class, args);
    }
}
