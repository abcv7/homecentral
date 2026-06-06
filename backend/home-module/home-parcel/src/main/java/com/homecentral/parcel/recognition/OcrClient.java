package com.homecentral.parcel.recognition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class OcrClient {

    private static final Logger log = LoggerFactory.getLogger(OcrClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String ocrUrl;

    public OcrClient(RestTemplate restTemplate, ObjectMapper objectMapper,
                     @Value("${app.ocr.url:}") String ocrUrl) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.ocrUrl = ocrUrl;
    }

    public String recognize(String imageBase64) {
        if (ocrUrl == null || ocrUrl.isBlank()) {
            log.warn("OCR not configured, skipping");
            return "";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("image_base64", imageBase64);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                ocrUrl + "/ocr/base64", HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseOcrResponse(response.getBody());
            }
            log.warn("OCR response not ok: {}", response.getStatusCode());
            return "";
        } catch (Exception e) {
            log.error("OCR recognition failed", e);
            return "";
        }
    }

    private String parseOcrResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode results = root.get("results");
            if (results == null || !results.isArray()) return responseBody;

            StringBuilder sb = new StringBuilder();
            for (JsonNode item : results) {
                JsonNode text = item.get("text");
                if (text != null && !text.isNull()) {
                    sb.append(text.asText()).append("\n");
                }
            }
            return sb.toString().trim();
        } catch (Exception e) {
            log.error("Failed to parse OCR response", e);
            return responseBody;
        }
    }
}
