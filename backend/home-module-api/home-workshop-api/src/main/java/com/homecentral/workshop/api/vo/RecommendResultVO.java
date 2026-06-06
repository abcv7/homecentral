package com.homecentral.workshop.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "推荐结果 (替代前端纯函数, 服务端计算后返回)")
public class RecommendResultVO {

    @Schema(description = "匹配度分级: FULL 完全匹配 / MISS_1 差1 / MISS_2 差2 / MISS_3 差3+")
    private String tier;

    private CocktailVO cocktail;
    private List<IngredientVO> missingIngredients;
    private List<IngredientVO> matchedIngredients;
    private BigDecimal missingCount;

    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }
    public CocktailVO getCocktail() { return cocktail; }
    public void setCocktail(CocktailVO cocktail) { this.cocktail = cocktail; }
    public List<IngredientVO> getMissingIngredients() { return missingIngredients; }
    public void setMissingIngredients(List<IngredientVO> missingIngredients) { this.missingIngredients = missingIngredients; }
    public List<IngredientVO> getMatchedIngredients() { return matchedIngredients; }
    public void setMatchedIngredients(List<IngredientVO> matchedIngredients) { this.matchedIngredients = matchedIngredients; }
    public BigDecimal getMissingCount() { return missingCount; }
    public void setMissingCount(BigDecimal missingCount) { this.missingCount = missingCount; }
}
