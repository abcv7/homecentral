package com.homecentral.notification.controller;

import com.homecentral.common.model.Result;
import com.homecentral.notification.api.dto.MailSendRequest;
import com.homecentral.notification.api.email.EmailMessage;
import com.homecentral.notification.service.EmailDispatcher;
import com.homecentral.notification.service.MailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "邮件通知", description = "内部调用，统一收口 QQ SMTP 发送")
@RestController
@RequestMapping("/api/notification")
public class MailController {

    private final MailService mailService;
    private final EmailDispatcher emailDispatcher;

    public MailController(MailService mailService, EmailDispatcher emailDispatcher) {
        this.mailService = mailService;
        this.emailDispatcher = emailDispatcher;
    }

    @Operation(summary = "发送邮件（内部 API，已渲染的 MailSendRequest）")
    @PostMapping("/mail")
    public Result<Void> send(@Valid @RequestBody MailSendRequest request) {
        mailService.send(request);
        return Result.<Void>ok(null);
    }

    @Operation(summary = "发送邮件（多态 EmailMessage，服务端渲染）",
               description = "消费方传入 EmailMessage 子类（验证码/通知），服务端按 type 路由到模板并渲染")
    @PostMapping("/send-message")
    public Result<Void> sendMessage(@RequestBody EmailMessage message) {
        emailDispatcher.dispatch(message);
        return Result.<Void>ok(null);
    }
}
