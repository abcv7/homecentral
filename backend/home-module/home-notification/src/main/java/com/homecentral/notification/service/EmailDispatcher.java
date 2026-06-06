package com.homecentral.notification.service;

import com.homecentral.notification.api.dto.MailSendRequest;
import com.homecentral.notification.api.email.EmailMessage;
import com.homecentral.notification.api.email.EmailChangeCodeEmail;
import com.homecentral.notification.api.email.FriendAcceptEmail;
import com.homecentral.notification.api.email.PasswordChangeCodeEmail;
import com.homecentral.notification.api.email.PurchaseNoticeEmail;
import org.springframework.stereotype.Component;

@Component
public class EmailDispatcher {

    private final EmailTemplateService templateService;
    private final MailService mailService;

    public EmailDispatcher(EmailTemplateService templateService, MailService mailService) {
        this.templateService = templateService;
        this.mailService = mailService;
    }

    public void dispatch(EmailMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("EmailMessage 不能为空");
        }
        if (message.getTo() == null || message.getTo().isBlank()) {
            throw new IllegalArgumentException("收件人不能为空");
        }
        MailSendRequest request = render(message);
        mailService.send(request);
    }

    public MailSendRequest render(EmailMessage message) {
        String templatePath = resolveTemplatePath(message);
        String subject = resolveSubject(message);
        String html = templateService.render(templatePath, message.toVariables());
        MailSendRequest req = new MailSendRequest();
        req.setTo(message.getTo());
        req.setSubject(subject);
        req.setHtmlBody(html);
        return req;
    }

    private String resolveTemplatePath(EmailMessage message) {
        if (message instanceof EmailChangeCodeEmail
            || message instanceof PasswordChangeCodeEmail) {
            return "email/verification-code.html";
        }
        if (message instanceof FriendAcceptEmail
            || message instanceof PurchaseNoticeEmail) {
            return "email/notification.html";
        }
        throw new IllegalArgumentException("未支持的邮件类型: " + message.getClass().getName());
    }

    private String resolveSubject(EmailMessage message) {
        String subject = message.getDefaultSubject();
        if (subject == null || subject.isBlank()) {
            throw new IllegalStateException("邮件主题为空: " + message.getClass().getSimpleName());
        }
        return subject;
    }
}
