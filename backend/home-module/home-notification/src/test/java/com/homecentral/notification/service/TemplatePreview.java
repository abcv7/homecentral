package com.homecentral.notification.service;

import com.homecentral.notification.api.email.EmailChangeCodeEmail;
import com.homecentral.notification.api.email.FriendAcceptEmail;
import com.homecentral.notification.api.email.PasswordChangeCodeEmail;
import com.homecentral.notification.api.email.PurchaseItem;
import com.homecentral.notification.api.email.PurchaseNoticeEmail;
import com.homecentral.notification.api.email.PurchaseNoticeEmail.Variant;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class TemplatePreview {

    @Test
    void renderToFile() throws Exception {
        EmailTemplateService svc = new EmailTemplateService();

        Path out = Path.of("target/preview");
        Files.createDirectories(out);

        Files.writeString(out.resolve("email-change-code.html"),
            svc.render("email/verification-code.html",
                new EmailChangeCodeEmail("abc***7@qq.com", "120462", 5, "abc***7@qq.com").toVariables()));

        Files.writeString(out.resolve("password-change-code.html"),
            svc.render("email/verification-code.html",
                new PasswordChangeCodeEmail("abc***7@qq.com", "888999", 5, "abc***7@qq.com").toVariables()));

        Files.writeString(out.resolve("friend-accept.html"),
            svc.render("email/notification.html",
                new FriendAcceptEmail("owner@qq.com", "家人", "老王", "小李", "xiaoli").toVariables()));

        Files.writeString(out.resolve("purchase-partner.html"),
            svc.render("email/notification.html",
                new PurchaseNoticeEmail("partner@example.com", Variant.PARTNER,
                    null, "老王", null,
                    List.of(new PurchaseItem("鸡蛋", 6, "个"), new PurchaseItem("西红柿", 3, "个"), new PurchaseItem("牛奶", 2, "瓶")))
                    .toVariables()));

        Files.writeString(out.resolve("purchase-group.html"),
            svc.render("email/notification.html",
                new PurchaseNoticeEmail("group-member@qq.com", Variant.GROUP,
                    "小李", "老王", "家人",
                    List.of(new PurchaseItem("酸奶", 4, "盒"), new PurchaseItem("苹果", 2, "个")))
                    .toVariables()));

        System.out.println("[preview] rendered templates to " + out.toAbsolutePath());
    }
}
