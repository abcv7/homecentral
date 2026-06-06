package com.homecentral.workshop.controller;

import com.homecentral.common.model.Result;
import com.homecentral.workshop.api.dto.RecommendRequest;
import com.homecentral.workshop.api.vo.RecommendResultVO;
import com.homecentral.workshop.service.IRecommendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "调酒台推荐", description = "调酒台匹配算法服务端实现 (替代前端纯函数 recommend)")
@RestController
@RequestMapping("/api/workshop/recommend")
public class RecommendController {

    private final IRecommendService recommendService;

    public RecommendController(IRecommendService recommendService) {
        this.recommendService = recommendService;
    }

    @Operation(summary = "调酒台推荐 (POST 避免长 query 串)")
    @PostMapping
    public Result<List<RecommendResultVO>> recommend(@RequestBody RecommendRequest request) {
        return Result.ok(recommendService.recommend(request));
    }
}
