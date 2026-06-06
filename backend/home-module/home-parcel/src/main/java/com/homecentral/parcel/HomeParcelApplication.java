package com.homecentral.parcel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableFeignClients(basePackages = {"com.homecentral.parcel", "com.homecentral.parcel.api.feign", "com.homecentral.friend.api.feign"})
@EnableScheduling
@SpringBootApplication(scanBasePackages = {"com.homecentral.parcel", "com.homecentral.ai", "com.homecentral.common", "com.homecentral.friend.api.feign"})
public class HomeParcelApplication {

    public static void main(String[] args) {
        SpringApplication.run(HomeParcelApplication.class, args);
    }
}
