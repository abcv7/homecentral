package com.homecentral.notification.api.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailMessageJacksonTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldSerializeAndDeserializeEmailChangeCode() throws Exception {
        EmailChangeCodeEmail original = new EmailChangeCodeEmail("user@qq.com", "654321", 5, "user@qq.com");
        String json = mapper.writeValueAsString(original);
        assertTrue(json.contains("\"type\":\"email_change_code\""));
        assertTrue(json.contains("\"code\":\"654321\""));

        EmailMessage restored = mapper.readValue(json, EmailMessage.class);
        assertTrue(restored instanceof EmailChangeCodeEmail);
        EmailChangeCodeEmail casted = (EmailChangeCodeEmail) restored;
        assertEquals("user@qq.com", casted.getTo());
        assertEquals("654321", casted.getCode());
        assertEquals(5, casted.getMinutes());
        assertEquals("邮箱变更", casted.getPurposeText());
        assertEquals("更换绑定邮箱", casted.getPurposeBadge());
    }

    @Test
    void shouldSerializeAndDeserializePasswordChangeCode() throws Exception {
        PasswordChangeCodeEmail original = new PasswordChangeCodeEmail("u@x.com", "111111", 5, "u@x.com");
        String json = mapper.writeValueAsString(original);
        assertTrue(json.contains("\"type\":\"password_change_code\""));

        EmailMessage restored = mapper.readValue(json, EmailMessage.class);
        assertTrue(restored instanceof PasswordChangeCodeEmail);
        PasswordChangeCodeEmail casted = (PasswordChangeCodeEmail) restored;
        assertEquals("111111", casted.getCode());
        assertEquals("密码变更", casted.getPurposeText());
        assertEquals("修改登录密码", casted.getPurposeBadge());
    }

    @Test
    void shouldSerializeAndDeserializeFriendAccept() throws Exception {
        FriendAcceptEmail original = new FriendAcceptEmail("owner@x.com", "家人", "老王", "小李", "xiaoli");
        String json = mapper.writeValueAsString(original);
        assertTrue(json.contains("\"type\":\"friend_accept\""));

        EmailMessage restored = mapper.readValue(json, EmailMessage.class);
        assertTrue(restored instanceof FriendAcceptEmail);
        FriendAcceptEmail casted = (FriendAcceptEmail) restored;
        assertEquals("家人", casted.getGroupName());
        assertEquals("小李", casted.getAccepterNickname());
    }

    @Test
    void shouldSerializeAndDeserializePurchaseNotice() throws Exception {
        PurchaseNoticeEmail original = new PurchaseNoticeEmail(
            "p@x.com", PurchaseNoticeEmail.Variant.PARTNER,
            null, null, null,
            List.of(new PurchaseItem("鸡蛋", 6, "个")));
        String json = mapper.writeValueAsString(original);
        assertTrue(json.contains("\"type\":\"purchase_notice\""));
        assertTrue(json.contains("\"variant\":\"PARTNER\""));

        EmailMessage restored = mapper.readValue(json, EmailMessage.class);
        assertTrue(restored instanceof PurchaseNoticeEmail);
        PurchaseNoticeEmail casted = (PurchaseNoticeEmail) restored;
        assertEquals(PurchaseNoticeEmail.Variant.PARTNER, casted.getVariant());
        assertEquals(1, casted.getItems().size());
        assertEquals("鸡蛋", casted.getItems().get(0).getName());
    }

    @Test
    void codeBoxesRendererShouldProduceSixCells() {
        String boxes = EmailRenderSupport.renderCodeBoxes("123456");
        int count = (boxes.length() - boxes.replace("<td", "").length()) / "<td".length();
        assertEquals(6, count);
    }
}
