package com.homecentral.fridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableFeignClients(basePackages = {
        "com.homecentral.fridge.api.feign",
        "com.homecentral.friend.api.feign",
        "com.homecentral.notification.api.feign",
        "com.homecentral.auth.api.feign"
})
@EnableScheduling
@SpringBootApplication(scanBasePackages = {
        "com.homecentral.fridge",
        "com.homecentral.ai",
        "com.homecentral.common"
})
public class HomeFridgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(HomeFridgeApplication.class, args);
    }
}
