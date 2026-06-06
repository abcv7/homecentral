package com.homecentral.fridge.recognition;

import com.homecentral.ai.AiClient;
import com.homecentral.fridge.api.vo.FridgeRecognizeVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 食材识别专用 VLM 薄壳。
 * <p>
 * 维护食材领域 system prompt，调用 {@link AiClient} 完成图片识别。
 * 输出为 {@link FridgeRecognizeVO} 列表，前端在表单中可二次编辑。
 */
@Service
public class FoodVlmClient {

    private static final Logger log = LoggerFactory.getLogger(FoodVlmClient.class);

    private static final String FOOD_SYSTEM_PROMPT = """
        你是食材识别专家。分析图片中所有可见的可食用食材 / 食品 / 调味品 / 饮品。

        支持场景：
        1. 打开的冰箱内部照片（含冷藏/冷冻/门上小件）
        2. 厨房台面上的食材/调味品
        3. 超市购物袋/购物篮中的商品
        4. 单独包装的食品（包装袋、罐头、瓶子、盒子等）

        以 JSON 数组返回，每个元素包含：
        - name: 食材名（具体，如"纯牛奶""西红柿""速冻饺子"；不要笼统的"食物"）
        - suggestedZone: REFRIGERATED（冷藏）或 FROZEN（冷冻）或 "AMBIENT"（常温）
            * 新鲜蔬菜/水果/乳制品/已开瓶饮料 → REFRIGERATED
            * 冷冻食品/速冻品/冰激凌 → FROZEN
            * 罐头/常温调料/未开封的饼干/方便面 → AMBIENT
        - suggestedSubZone: 建议子区域（自由文本），如"冷藏-门上-左""冷藏-中层""冷冻-抽屉1"，无法判断填空字符串
        - suggestedCategoryName: 推荐分类（中文），必须从以下 10 类选择最接近的：
            酒水 / 蔬菜 / 水果 / 生鲜 / 乳制品 / 肉类 / 调味品 / 主食 / 零食 / 其他
        - estimatedQuantity: 数量（数字，可为 0.5/1/2 等小数；单件时为 1）
        - estimatedUnit: 单位（个/瓶/袋/盒/克/千克/毫升/升；选用最自然的）
        - estimatedExpiryDays: 从今天起预计可保存的天数（已开/新鲜 3-7，冷冻 30-180，罐头/干货 180-365）
        - notes: 备注（识别依据、是否含包装等）

        规则：
        1. 多个独立食材 → 多个数组元素；同类多个（如 5 个鸡蛋）合并为 1 个元素，quantity=5
        2. 字段不确定时用 null 而不是猜测
        3. 同一张照片中能看到的都列出，不要遗漏
        4. 如果图片中没有食材 / 食品 / 调味品 / 饮品，返回空数组 []
        5. 直接返回 JSON 数组，不要包含 markdown 代码块或其他文本
        """;

    private final AiClient aiClient;

    public FoodVlmClient(AiClient aiClient) {
        this.aiClient = aiClient;
    }

    public List<FridgeRecognizeVO> recognize(String imageBase64) {
        long start = System.currentTimeMillis();
        List<FridgeRecognizeVO> raw = aiClient.recognizeImage(
                imageBase64,
                FOOD_SYSTEM_PROMPT,
                "请识别这张图片中的所有食材/食品/调味品/饮品，并按 schema 返回结构化结果。",
                FridgeRecognizeVO.class);
        log.info("FoodVlmClient.recognize returned {} items in {}ms",
                raw.size(), System.currentTimeMillis() - start);

        raw.forEach(vo -> {
            if (vo.getConfidence() == null) {
                vo.setConfidence("MEDIUM");
            }
        });
        return raw;
    }
}
