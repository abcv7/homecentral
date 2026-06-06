package com.homecentral.workshop.service;

import com.homecentral.workshop.api.dto.RecommendRequest;
import com.homecentral.workshop.api.vo.RecommendResultVO;

import java.util.List;

public interface IRecommendService {

    /**
     * 调酒台推荐
     * <p>
     * 端口 src/main/resources/frontend WebPage/src/utils/workshop-recommend.ts 的 recommend() 纯函数。
     * 算法不变:tier 优先 -> ratio 降序 -> views 降序;0 命中过滤;主料模式空集过滤。
     *
     * @param request ingredientIds + mode(STRICT/MAIN)
     * @return 按规则排序后的推荐结果(可能空集)
     */
    List<RecommendResultVO> recommend(RecommendRequest request);
}
