package com.homecentral.notification.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "发送邮件请求")
public class MailSendRequest {

    @NotBlank
    @Email
    @Schema(description = "收件人邮箱", example = "partner@example.com")
    private String to;

    @NotBlank
    @Schema(description = "主题")
    private String subject;

    @NotBlank
    @Schema(description = "HTML 格式邮件正文")
    private String htmlBody;

    @Schema(description = "纯文本正文（可选，部分客户端不支持 HTML 时使用）")
    private String textBody;

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getHtmlBody() { return htmlBody; }
    public void setHtmlBody(String htmlBody) { this.htmlBody = htmlBody; }
    public String getTextBody() { return textBody; }
    public void setTextBody(String textBody) { this.textBody = textBody; }
}
