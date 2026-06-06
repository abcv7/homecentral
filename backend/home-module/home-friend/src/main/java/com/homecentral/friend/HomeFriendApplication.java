package com.homecentral.friend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = {
        "com.homecentral.friend.api.feign",
        "com.homecentral.auth.api.feign",
        "com.homecentral.notification.api.feign"
})
@MapperScan("com.homecentral.friend.mapper")
@SpringBootApplication(scanBasePackages = {"com.homecentral.friend", "com.homecentral.common"})
public class HomeFriendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HomeFriendApplication.class, args);
    }
}
