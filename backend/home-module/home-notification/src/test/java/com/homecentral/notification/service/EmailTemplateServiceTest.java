package com.homecentral.notification.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class EmailTemplateServiceTest {

    private final EmailTemplateService service = new EmailTemplateService();

    @Test
    void shouldThrowWhenTemplatePathBlank() {
        assertThrows(IllegalArgumentException.class, () -> service.render("", new HashMap<>()));
        assertThrows(IllegalArgumentException.class, () -> service.render(null, new HashMap<>()));
    }

    @Test
    void shouldThrowWhenTemplateMissing() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.render("email/not-exist.html", new HashMap<>()));
        assertTrue(ex.getMessage().contains("邮件模板不存在"));
    }

    @Test
    void shouldReplacePlaceholdersFromInlineTemplate() {
        Map<String, Object> vars = Map.of("name", "张三", "code", "123456");
        String html = service.render("email/test/sample.html", vars);
        assertTrue(html.contains("Hi 张三"));
        assertTrue(html.contains("code: 123456"));
        assertTrue(!html.contains("{{"));
    }

    @Test
    void shouldLeaveBlankForMissingVariable() {
        String html = service.render("email/test/sample.html", Map.of("name", "李四"));
        assertTrue(html.contains("Hi 李四"));
        assertTrue(html.contains("code: "));
    }
}
