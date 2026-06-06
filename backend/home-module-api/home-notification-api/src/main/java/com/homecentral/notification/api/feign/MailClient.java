package com.homecentral.notification.api.feign;

import com.homecentral.common.model.Result;
import com.homecentral.notification.api.dto.MailSendRequest;
import com.homecentral.notification.api.email.EmailMessage;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "home-notification", path = "/api/notification")
public interface MailClient {

    @PostMapping("/mail")
    Result<Void> send(@RequestBody MailSendRequest request);

    @PostMapping("/send-message")
    Result<Void> sendMessage(@RequestBody EmailMessage message);
}
