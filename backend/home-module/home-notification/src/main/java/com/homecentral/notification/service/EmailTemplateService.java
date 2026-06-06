package com.homecentral.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EmailTemplateService implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(EmailTemplateService.class);
    private static final String TEMPLATE_PREFIX = "templates/";
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{\\s*([a-zA-Z0-9_]+)\\s*}}");

    private final Map<String, String> templateCache = new ConcurrentHashMap<>();

    @Override
    public void afterPropertiesSet() {
        log.info("[email-template] 模板服务已就绪（classpath:{}）", TEMPLATE_PREFIX);
    }

    public String render(String templatePath, Map<String, Object> variables) {
        String raw = loadTemplate(templatePath);
        if (variables == null) {
            variables = new HashMap<>();
        }
        Map<String, Object> safeVars = new HashMap<>(variables);
        Matcher matcher = PLACEHOLDER.matcher(raw);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String name = matcher.group(1);
            Object value = safeVars.get(name);
            String replacement = value == null ? "" : Matcher.quoteReplacement(value.toString());
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String loadTemplate(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("templatePath 不能为空");
        }
        String normalized = path.startsWith(TEMPLATE_PREFIX) ? path : TEMPLATE_PREFIX + path;
        return templateCache.computeIfAbsent(normalized, this::readResource);
    }

    private String readResource(String path) {
        try {
            Resource resource = new ClassPathResource(path);
            if (!resource.exists()) {
                throw new IllegalStateException("邮件模板不存在: " + path);
            }
            String content = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            log.debug("[email-template] 加载模板 {}", path);
            return content;
        } catch (IOException e) {
            throw new IllegalStateException("读取邮件模板失败: " + path, e);
        }
    }
}
