package com.homecentral.workshop.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "推荐请求体 (替代前端纯函数 recommend, 计算下沉到后端)")
public class RecommendRequest {

    @Schema(description = "用户拥有的原料 id 列表", example = "[1, 4, 12]")
    private java.util.List<Long> ingredientIds;

    @Schema(description = "推荐模式: STRICT 严格 (用户原料 ∩ cocktail 原料 == 用户原料), MAIN 主料 (子集匹配, 优先匹配主料)",
            example = "MAIN", allowableValues = {"STRICT", "MAIN"})
    private String mode = "MAIN";

    public java.util.List<Long> getIngredientIds() { return ingredientIds; }
    public void setIngredientIds(java.util.List<Long> ingredientIds) { this.ingredientIds = ingredientIds; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
}
