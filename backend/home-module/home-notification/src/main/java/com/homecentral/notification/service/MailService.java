package com.homecentral.notification.service;

import com.homecentral.notification.api.dto.MailSendRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;

    @Value("${home.notification.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${spring.mail.username:abcv6@qq.com}")
    private String fromAddress;

    @Value("${home.notification.mail.from-nickname:栖物集}")
    private String fromNickname;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void send(MailSendRequest request) {
        if (!mailEnabled) {
            log.info("[mail-stub] to={} subject={} body={}", request.getTo(), request.getSubject(),
                    truncate(request.getHtmlBody(), 200));
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(buildFromAddress());
            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());
            helper.setText(request.getHtmlBody(), true);
            if (request.getTextBody() != null) {
                helper.setText(request.getTextBody());
            }
            mailSender.send(message);
            log.info("[mail-sent] to={} subject={}", request.getTo(), request.getSubject());
        } catch (Exception e) {
            log.error("[mail-error] to={} subject={} ({})", request.getTo(), request.getSubject(), e.getMessage());
            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
        }
    }

    private InternetAddress buildFromAddress() {
        String nickname = (fromNickname == null || fromNickname.isBlank()) ? "栖物集" : fromNickname;
        try {
            InternetAddress addr = new InternetAddress(fromAddress);
            addr.setPersonal(nickname, StandardCharsets.UTF_8.name());
            return addr;
        } catch (Exception e) {
            log.warn("[mail-from] 昵称编码失败，降级为纯邮箱: {}", e.getMessage());
            try {
                return new InternetAddress(fromAddress);
            } catch (Exception ex) {
                throw new RuntimeException("发件人地址非法: " + fromAddress, ex);
            }
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) + "..." : s;
    }
}
