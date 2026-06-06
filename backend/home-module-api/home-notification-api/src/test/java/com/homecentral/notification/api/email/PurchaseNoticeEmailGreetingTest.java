package com.homecentral.notification.api.email;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PurchaseNoticeEmailGreetingTest {

    @Test
    void shouldUseFormalGreetingWhenRecipientNicknameIsBlank() {
        PurchaseNoticeEmail email = new PurchaseNoticeEmail(
            "external@qq.com",
            PurchaseNoticeEmail.Variant.PARTNER,
            null,
            "老王",
            null,
            List.of(new PurchaseItem("鸡蛋", 6, "个"))
        );

        Map<String, Object> vars = email.toVariables();
        assertEquals("您好，", vars.get("greeting"));
    }

    @Test
    void shouldUseFormalGreetingWhenRecipientNicknameIsEmpty() {
        PurchaseNoticeEmail email = new PurchaseNoticeEmail(
            "external@qq.com",
            PurchaseNoticeEmail.Variant.PARTNER,
            "  ",
            "老王",
            null,
            List.of(new PurchaseItem("鸡蛋", 6, "个"))
        );

        Map<String, Object> vars = email.toVariables();
        assertEquals("您好，", vars.get("greeting"));
    }

    @Test
    void shouldUsePersonalGreetingWhenRecipientNicknamePresent() {
        PurchaseNoticeEmail email = new PurchaseNoticeEmail(
            "member@qq.com",
            PurchaseNoticeEmail.Variant.GROUP,
            "小李",
            "老王",
            "家人",
            List.of(new PurchaseItem("酸奶", 4, "盒"))
        );

        Map<String, Object> vars = email.toVariables();
        assertEquals("Hi 小李，", vars.get("greeting"));
    }
}
