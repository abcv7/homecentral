package com.homecentral.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

/**
 * 公共 AI 客户端，封装 Spring AI ChatClient 的多模态调用。
 * <p>
 * 提供两类通用能力：
 * <ul>
 *     <li>{@link #recognizeImage(String, String, String, Class)} - 上传图片 + 提示词，返回结构化结果列表</li>
 *     <li>{@link #recognizeText(String, String, String, Class)} - 纯文本 + 提示词，返回结构化结果列表</li>
 * </ul>
 * <p>
 * 调用方只需提供 systemPrompt / userPrompt / 期望的元素类型 T。底层：
 * <ol>
 *     <li>图像自动压缩到 1024 像素宽，节省 VLM token</li>
 *     <li>响应剥离 markdown 代码块后解析为 JSON 数组</li>
 *     <li>解析失败/空响应统一返回空列表，业务侧无需 try-catch</li>
 * </ol>
 * <p>
 * 配置位于 infra/infra-config.yml 的 spring.ai.openai.*，由 spring-ai-starter-model-openai
 * 自动注入 ChatClient.Builder。
 */
@Component
public class AiClient {

    private static final Logger log = LoggerFactory.getLogger(AiClient.class);

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    private static final int MAX_WIDTH = 1024;

    public AiClient(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 调用 VLM 识别图片内容。
     *
     * @param imageBase64  图片 base64 编码（无 data:image/xxx;base64, 前缀）
     * @param systemPrompt 系统提示词，约束模型角色与输出 schema
     * @param userPrompt   用户提示词，描述当次任务
     * @param elementType  期望的列表元素类型（如 ParcelRecognitionVO.class）
     * @param <T>          元素类型
     * @return 识别结果列表，失败时返回空列表（非 null）
     */
    public <T> List<T> recognizeImage(String imageBase64, String systemPrompt, String userPrompt, Class<T> elementType) {
        long start = System.currentTimeMillis();
        try {
            String compressed = compressImage(imageBase64);
            log.info("Image compressed: {} -> {} bytes (ratio {:.0f}%)",
                    imageBase64.length(), compressed.length(),
                    (1.0 - (double) compressed.length() / imageBase64.length()) * 100);

            byte[] imageBytes = Base64.getDecoder().decode(compressed);

            String response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(u -> u.text(userPrompt)
                            .media(new Media(MimeTypeUtils.IMAGE_JPEG, new ByteArrayResource(imageBytes))))
                    .call()
                    .content();

            long elapsed = System.currentTimeMillis() - start;
            log.info("VLM image recognition completed in {}ms", elapsed);
            log.debug("VLM raw response: {}", response);

            return parseJsonArray(response, elementType);
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("VLM image recognition failed after {}ms", elapsed, e);
            return Collections.emptyList();
        }
    }

    /**
     * 调用 LLM 识别纯文本内容（不传图片）。
     *
     * @param text         原始文本
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户提示词
     * @param elementType  期望的列表元素类型
     * @param <T>          元素类型
     * @return 识别结果列表
     */
    public <T> List<T> recognizeText(String text, String systemPrompt, String userPrompt, Class<T> elementType) {
        long start = System.currentTimeMillis();
        try {
            String response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt + "\n\n" + text)
                    .call()
                    .content();

            long elapsed = System.currentTimeMillis() - start;
            log.info("LLM text recognition completed in {}ms", elapsed);
            log.debug("LLM raw response: {}", response);

            return parseJsonArray(response, elementType);
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("LLM text recognition failed after {}ms", elapsed, e);
            return Collections.emptyList();
        }
    }

    /**
     * 解析模型返回的 JSON 数组。
     * 容忍 ```json``` / ``` 包裹的 markdown 代码块。
     */
    public <T> List<T> parseJsonArray(String responseBody, Class<T> elementType) {
        if (responseBody == null || responseBody.isBlank()) {
            log.warn("Model response is empty");
            return Collections.emptyList();
        }

        try {
            String content = stripMarkdownCodeBlock(responseBody.strip());

            JsonNode root = objectMapper.readTree(content);
            JsonNode arrayNode = root.isArray() ? root : null;
            if (arrayNode == null) {
                log.warn("Model response is not a JSON array, first 200 chars: {}",
                        content.substring(0, Math.min(content.length(), 200)));
                return Collections.emptyList();
            }

            List<T> results = new ArrayList<>();
            for (JsonNode item : arrayNode) {
                T mapped = objectMapper.treeToValue(item, elementType);
                results.add(mapped);
            }
            log.info("Parsed {} items from model response", results.size());
            return results;
        } catch (Exception e) {
            log.error("Failed to parse model response, first 500 chars: {}",
                    responseBody.substring(0, Math.min(responseBody.length(), 500)), e);
            return Collections.emptyList();
        }
    }

    /**
     * 压缩 base64 编码的图片到指定宽度上限，保持原比例。
     * 若原图已 <= 1024px 或解码失败则原样返回。
     */
    public String compressImage(String imageBase64) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(imageBase64);
            BufferedImage original = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (original == null) {
                return imageBase64;
            }

            int width = original.getWidth();
            if (width <= MAX_WIDTH) {
                return imageBase64;
            }

            double ratio = (double) MAX_WIDTH / width;
            int height = (int) (original.getHeight() * ratio);
            BufferedImage resized = new BufferedImage(MAX_WIDTH, height, BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = resized.createGraphics();
            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                    java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(original.getScaledInstance(MAX_WIDTH, height, java.awt.Image.SCALE_SMOOTH), 0, 0, null);
            g.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resized, "jpg", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            log.warn("Image compression failed, using original: {}", e.getMessage());
            return imageBase64;
        }
    }

    private String stripMarkdownCodeBlock(String content) {
        if (content.startsWith("```json")) {
            content = content.substring(7);
        } else if (content.startsWith("```")) {
            content = content.substring(3);
        }
        if (content.endsWith("```")) {
            content = content.substring(0, content.length() - 3);
        }
        return content.strip();
    }
}
