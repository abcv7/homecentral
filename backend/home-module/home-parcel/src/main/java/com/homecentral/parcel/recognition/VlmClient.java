package com.homecentral.parcel.recognition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.homecentral.ai.AiClient;
import com.homecentral.parcel.api.vo.ParcelRecognitionVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 包裹识别专用 VLM 薄壳。
 * <p>
 * 调用公共的 {@link AiClient} 完成图片/文本识别，仅负责：
 * <ol>
 *     <li>维护快递领域的 system prompt</li>
 *     <li>为识别结果填充 confidence / fieldConfidence 字段</li>
 * </ol>
 * <p>
 * 通用能力（图片压缩、JSON 解析、Markdown 剥离）由 AiClient 统一提供。
 */
@Service
public class VlmClient {

    private static final Logger log = LoggerFactory.getLogger(VlmClient.class);

    private static final String PARCEL_SYSTEM_PROMPT = """
        你是快递信息识别专家。分析图片中所有可见的快递/包裹信息。

        支持以下图片类型：
        1. 快递面单 — 提取快递公司、运单号、收件人
        2. 短信截图（如妈妈驿站/菜鸟驿站取件通知）— 提取站点名、取件码、地址
        3. 电商订单页（如拼多多/淘宝/京东"我的订单"）— 提取快递公司、运单号、取件码、商品名
        4. 物流详情页 — 提取快递公司、运单号、当前状态

        以 JSON 数组返回，每个元素包含：
        - courierCompany: 快递公司（顺丰速运/圆通速递/中通快递/韵达快递/申通快递/极兔速递/邮政快递/妈妈驿站/菜鸟驿站/其他）
        - trackingNumber: 运单号（如可见，纯数字或字母数字组合）
        - pickupCode: 取件码（格式通常为 X-X-XXXX，如 1-1-0048）
        - ownerName: 收件人（如可见）
        - arrivedDate: 到达日期 YYYY-MM-DD（如可推断，今天填今天日期）
        - productName: 商品名称（从图片推断，无法识别填"未知"）

        规则：
        1. 每个独立的快递/包裹创建一个数组元素
        2. 字段不可见则设为 null
        3. 商品名称尽量具体（如"电蚊拍"、"厚椰乳"）
        4. 如果是短信截图，快递公司就是短信中的站点名（如"妈妈驿站"）
        5. 如果是订单页，每个商品行是一个独立包裹
        6. 如果图片中没有快递信息，返回空数组 []
        7. 直接返回 JSON 数组，不要包含 markdown 代码块或其他文本
        """;

    private static final String PARCEL_SMS_SYSTEM_PROMPT = """
        你是快递信息识别专家。分析以下短信/文本中的快递包裹信息。

        以 JSON 数组返回，每个元素包含：
        - courierCompany: 快递公司或驿站名称
        - trackingNumber: 运单号（如可见）
        - pickupCode: 取件码（格式通常为 X-X-XXXX）
        - ownerName: 收件人（如可见）
        - arrivedDate: 到达日期 YYYY-MM-DD（如可推断）
        - productName: 商品名称（如可见，否则填"未知"）

        规则：
        1. 每个独立的快递/包裹创建一个数组元素
        2. 字段不可见则设为 null
        3. 取件码格式通常是 数字-数字-数字（如 1-1-0048）
        4. 短信中的站点名就是快递公司（如"妈妈驿站"、"菜鸟驿站"）
        5. 如果文本中没有快递信息，返回空数组 []
        6. 直接返回 JSON 数组，不要包含 markdown 代码块或其他文本
        """;

    private final AiClient aiClient;
    private final ObjectMapper objectMapper;

    public VlmClient(AiClient aiClient, ObjectMapper objectMapper) {
        this.aiClient = aiClient;
        this.objectMapper = objectMapper;
    }

    public List<ParcelRecognitionVO> recognize(String imageBase64) {
        List<ParcelRecognitionVO> raw = aiClient.recognizeImage(
                imageBase64,
                PARCEL_SYSTEM_PROMPT,
                "请识别这张图片中的所有快递信息，包括取件码、快递公司、运单号、商品名等",
                ParcelRecognitionVO.class);

        List<ParcelRecognitionVO> enriched = new java.util.ArrayList<>();
        for (ParcelRecognitionVO vo : raw) {
            vo.setFieldConfidence(new HashMap<>());
            vo.setConfidence("MEDIUM");
            log.info("  Parcel: company={}, tracking={}, pickup={}, product={}",
                    vo.getCourierCompany(), vo.getTrackingNumber(),
                    vo.getPickupCode(), vo.getProductName());
            enriched.add(vo);
        }
        return enriched;
    }

    public List<ParcelRecognitionVO> recognizeText(String smsText) {
        List<ParcelRecognitionVO> raw = aiClient.recognizeText(
                smsText,
                PARCEL_SMS_SYSTEM_PROMPT,
                "请识别以下短信中的快递信息：",
                ParcelRecognitionVO.class);

        List<ParcelRecognitionVO> enriched = new java.util.ArrayList<>();
        for (ParcelRecognitionVO vo : raw) {
            vo.setFieldConfidence(new HashMap<>());
            vo.setConfidence("MEDIUM");
            enriched.add(vo);
        }
        return enriched;
    }
}
