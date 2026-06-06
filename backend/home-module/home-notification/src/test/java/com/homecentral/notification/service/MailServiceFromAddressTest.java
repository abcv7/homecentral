package com.homecentral.notification.service;

import jakarta.mail.internet.InternetAddress;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class MailServiceFromAddressTest {

    @Test
    void shouldBuildInternetAddressWithUtf8Nickname() throws Exception {
        MailService svc = new MailService(mock(JavaMailSender.class));
        setField(svc, "fromAddress", "abcv6@qq.com");
        setField(svc, "fromNickname", "栖物集");

        Method m = MailService.class.getDeclaredMethod("buildFromAddress");
        m.setAccessible(true);
        InternetAddress addr = (InternetAddress) m.invoke(svc);

        assertNotNull(addr);
        assertEquals("abcv6@qq.com", addr.getAddress());
        assertEquals("栖物集", addr.getPersonal());
    }

    @Test
    void shouldFallbackToDefaultNicknameWhenBlank() throws Exception {
        MailService svc = new MailService(mock(JavaMailSender.class));
        setField(svc, "fromAddress", "abcv6@qq.com");
        setField(svc, "fromNickname", "  ");

        Method m = MailService.class.getDeclaredMethod("buildFromAddress");
        m.setAccessible(true);
        InternetAddress addr = (InternetAddress) m.invoke(svc);

        assertEquals("abcv6@qq.com", addr.getAddress());
        assertEquals("栖物集", addr.getPersonal());
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        java.lang.reflect.Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }
}
