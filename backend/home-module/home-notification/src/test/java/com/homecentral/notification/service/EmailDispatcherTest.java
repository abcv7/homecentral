package com.homecentral.notification.service;

import com.homecentral.notification.api.email.EmailChangeCodeEmail;
import com.homecentral.notification.api.email.FriendAcceptEmail;
import com.homecentral.notification.api.email.PasswordChangeCodeEmail;
import com.homecentral.notification.api.email.PurchaseItem;
import com.homecentral.notification.api.email.PurchaseNoticeEmail;
import com.homecentral.notification.api.email.PurchaseNoticeEmail.Variant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailDispatcherTest {

    @Mock
    private MailService mailService;

    private EmailTemplateService templateService = new EmailTemplateService();

    @org.junit.jupiter.api.BeforeEach
    void init() throws Exception {
        java.lang.reflect.Field f = EmailTemplateService.class.getDeclaredField("templateCache");
        f.setAccessible(true);
    }

    @Test
    void shouldRenderEmailChangeCodeAndDispatch() {
        EmailDispatcher dispatcher = new EmailDispatcher(templateService, mailService);
        EmailChangeCodeEmail msg = new EmailChangeCodeEmail("user@qq.com", "654321", 5, "user@qq.com");

        dispatcher.dispatch(msg);

        ArgumentCaptor<com.homecentral.notification.api.dto.MailSendRequest> captor =
            ArgumentCaptor.forClass(com.homecentral.notification.api.dto.MailSendRequest.class);
        verify(mailService).send(captor.capture());
        var req = captor.getValue();
        assertEquals("user@qq.com", req.getTo());
        assertEquals("【栖物集】您正在更换绑定邮箱", req.getSubject());
        assertTrue(req.getHtmlBody().contains("VERIFICATION&nbsp;CODE"));
        assertTrue(req.getHtmlBody().contains("更换绑定邮箱"));
        assertTrue(req.getHtmlBody().contains("6"));
        assertTrue(req.getHtmlBody().contains("5"));
        assertTrue(req.getHtmlBody().contains("5"));
    }

    @Test
    void shouldRenderPasswordChangeCodeAndDispatch() {
        EmailDispatcher dispatcher = new EmailDispatcher(templateService, mailService);
        PasswordChangeCodeEmail msg = new PasswordChangeCodeEmail("u@x.com", "111111", 5, "u@x.com");

        dispatcher.dispatch(msg);

        ArgumentCaptor<com.homecentral.notification.api.dto.MailSendRequest> captor =
            ArgumentCaptor.forClass(com.homecentral.notification.api.dto.MailSendRequest.class);
        verify(mailService).send(captor.capture());
        assertEquals("【栖物集】您正在修改登录密码", captor.getValue().getSubject());
        assertTrue(captor.getValue().getHtmlBody().contains("修改登录密码"));
    }

    @Test
    void shouldRenderFriendAcceptAndDispatch() {
        EmailDispatcher dispatcher = new EmailDispatcher(templateService, mailService);
        FriendAcceptEmail msg = new FriendAcceptEmail(
            "owner@qq.com", "家人", "老王", "小李", "xiaoli");

        dispatcher.dispatch(msg);

        ArgumentCaptor<com.homecentral.notification.api.dto.MailSendRequest> captor =
            ArgumentCaptor.forClass(com.homecentral.notification.api.dto.MailSendRequest.class);
        verify(mailService).send(captor.capture());
        var req = captor.getValue();
        assertEquals("【栖物集】好友接受了您的邀请", req.getSubject());
        assertTrue(req.getHtmlBody().contains("小李"));
        assertTrue(req.getHtmlBody().contains("家人"));
    }

    @Test
    void shouldRenderPurchaseNoticeWithItemsTable() {
        EmailDispatcher dispatcher = new EmailDispatcher(templateService, mailService);
        PurchaseNoticeEmail msg = new PurchaseNoticeEmail(
            "p@x.com",
            Variant.PARTNER,
            null, null, null,
            List.of(new PurchaseItem("鸡蛋", 6, "个"), new PurchaseItem("牛奶", 2, "瓶")));

        dispatcher.dispatch(msg);

        ArgumentCaptor<com.homecentral.notification.api.dto.MailSendRequest> captor =
            ArgumentCaptor.forClass(com.homecentral.notification.api.dto.MailSendRequest.class);
        verify(mailService).send(captor.capture());
        var body = captor.getValue().getHtmlBody();
        assertTrue(body.contains("鸡蛋"));
        assertTrue(body.contains("牛奶"));
        assertTrue(body.contains("6"));
        assertTrue(body.contains("瓶"));
    }

    @Test
    void shouldThrowWhenEmailMessageNull() {
        EmailDispatcher dispatcher = new EmailDispatcher(templateService, mailService);
        assertThrows(IllegalArgumentException.class, () -> dispatcher.dispatch(null));
    }

    @Test
    void shouldThrowWhenToBlank() {
        EmailDispatcher dispatcher = new EmailDispatcher(templateService, mailService);
        EmailChangeCodeEmail msg = new EmailChangeCodeEmail("", "123456", 5, "x");
        assertThrows(IllegalArgumentException.class, () -> dispatcher.dispatch(msg));
    }

    @Test
    void shouldPropagateMailServiceException() {
        EmailDispatcher dispatcher = new EmailDispatcher(templateService, mailService);
        EmailChangeCodeEmail msg = new EmailChangeCodeEmail("u@x.com", "123456", 5, "u@x.com");
        doThrow(new RuntimeException("smtp down")).when(mailService).send(any());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> dispatcher.dispatch(msg));
        assertNotNull(ex.getMessage());
    }
}
