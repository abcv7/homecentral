package com.homecentral.parcel.recognition;

import com.homecentral.parcel.api.vo.ParcelRecognitionVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RecognitionMerger {

    private static final Logger log = LoggerFactory.getLogger(RecognitionMerger.class);

    private static final Pattern TRACKING_PATTERN = Pattern.compile(
        "\\b(SF|YT|ZTO|STO|YD|JT|EMS|JD|JDVA|RNA|DBL|UBI|CN|PC)\\d{10,15}\\b",
        Pattern.CASE_INSENSITIVE);

    private static final Pattern PICKUP_CODE_PATTERN = Pattern.compile(
        "\\d{1,2}-\\d{1,2}-\\d{3,6}");

    private static final Map<String, String> COURIER_MAP = Map.ofEntries(
        Map.entry("顺丰", "顺丰速运"),
        Map.entry("SF", "顺丰速运"),
        Map.entry("圆通", "圆通速递"),
        Map.entry("YT", "圆通速递"),
        Map.entry("中通", "中通快递"),
        Map.entry("ZTO", "中通快递"),
        Map.entry("韵达", "韵达快递"),
        Map.entry("YD", "韵达快递"),
        Map.entry("申通", "申通快递"),
        Map.entry("STO", "申通快递"),
        Map.entry("极兔", "极兔速递"),
        Map.entry("极兔速递", "极兔速递"),
        Map.entry("JT", "极兔速递"),
        Map.entry("邮政", "邮政快递"),
        Map.entry("EMS", "邮政快递"),
        Map.entry("妈妈驿站", "妈妈驿站"),
        Map.entry("菜鸟驿站", "菜鸟驿站"),
        Map.entry("丰巢", "丰巢"),
        Map.entry("京东", "京东物流"),
        Map.entry("JD", "京东物流")
    );

    public List<ParcelRecognitionVO> merge(List<ParcelRecognitionVO> vlmItems, String ocrText) {
        if (vlmItems == null) vlmItems = new ArrayList<>();

        for (ParcelRecognitionVO vo : vlmItems) {
            Map<String, String> confidence = vo.getFieldConfidence() != null
                ? new HashMap<>(vo.getFieldConfidence()) : new HashMap<>();

            // 校验 trackingNumber
            if (vo.getTrackingNumber() != null && ocrText != null && !ocrText.isEmpty()) {
                if (ocrText.contains(vo.getTrackingNumber())) {
                    confidence.put("trackingNumber", "HIGH");
                } else {
                    confidence.put("trackingNumber", "MEDIUM");
                }
            } else if (vo.getTrackingNumber() != null) {
                confidence.put("trackingNumber", "MEDIUM");
            }

            // 校验 pickupCode
            if (vo.getPickupCode() != null && ocrText != null && !ocrText.isEmpty()) {
                if (ocrText.contains(vo.getPickupCode())) {
                    confidence.put("pickupCode", "HIGH");
                } else {
                    confidence.put("pickupCode", "MEDIUM");
                }
            } else if (vo.getPickupCode() != null) {
                confidence.put("pickupCode", "MEDIUM");
            }

            // 标准化快递公司名称
            if (vo.getCourierCompany() != null) {
                String normalized = normalizeCourierCompany(vo.getCourierCompany());
                vo.setCourierCompany(normalized);
                confidence.put("courierCompany", "HIGH");
            }

            // productName 仅 VLM 能提供
            if (vo.getProductName() != null) {
                confidence.put("productName", "HIGH");
            } else {
                vo.setProductName("未知");
                confidence.put("productName", "LOW");
            }

            // 计算总体置信度
            vo.setFieldConfidence(confidence);
            vo.setConfidence(calculateOverallConfidence(confidence));
        }

        return vlmItems;
    }

    private String normalizeCourierCompany(String raw) {
        if (raw == null) return null;
        String trimmed = raw.trim();
        if (COURIER_MAP.containsKey(trimmed)) {
            return COURIER_MAP.get(trimmed);
        }
        for (Map.Entry<String, String> entry : COURIER_MAP.entrySet()) {
            if (trimmed.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return trimmed;
    }

    private String calculateOverallConfidence(Map<String, String> fieldConfidence) {
        if (fieldConfidence.isEmpty()) return "LOW";
        long highCount = fieldConfidence.values().stream().filter("HIGH"::equals).count();
        long total = fieldConfidence.size();
        if (highCount == total) return "HIGH";
        if (highCount >= total / 2) return "MEDIUM";
        return "LOW";
    }

    public static List<String> extractPickupCodesFromOcr(String ocrText) {
        if (ocrText == null || ocrText.isEmpty()) return List.of();
        List<String> codes = new ArrayList<>();
        Matcher m = PICKUP_CODE_PATTERN.matcher(ocrText);
        while (m.find()) {
            codes.add(m.group());
        }
        return codes;
    }
}
