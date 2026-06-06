package com.homecentral.workshop.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "鸡尾酒聚合评分 (服务端计算 + 24h Redis 缓存)")
public class CocktailRatingSummaryVO {

    private Long cocktailId;
    private String cocktailNameZh;
    private Long ratingCount;
    private BigDecimal tasteAvg;
    private BigDecimal appearanceAvg;
    private BigDecimal overallAvg;
    private List<OrderRatingVO> latestRatings;

    public Long getCocktailId() { return cocktailId; }
    public void setCocktailId(Long cocktailId) { this.cocktailId = cocktailId; }
    public String getCocktailNameZh() { return cocktailNameZh; }
    public void setCocktailNameZh(String cocktailNameZh) { this.cocktailNameZh = cocktailNameZh; }
    public Long getRatingCount() { return ratingCount; }
    public void setRatingCount(Long ratingCount) { this.ratingCount = ratingCount; }
    public BigDecimal getTasteAvg() { return tasteAvg; }
    public void setTasteAvg(BigDecimal tasteAvg) { this.tasteAvg = tasteAvg; }
    public BigDecimal getAppearanceAvg() { return appearanceAvg; }
    public void setAppearanceAvg(BigDecimal appearanceAvg) { this.appearanceAvg = appearanceAvg; }
    public BigDecimal getOverallAvg() { return overallAvg; }
    public void setOverallAvg(BigDecimal overallAvg) { this.overallAvg = overallAvg; }
    public List<OrderRatingVO> getLatestRatings() { return latestRatings; }
    public void setLatestRatings(List<OrderRatingVO> latestRatings) { this.latestRatings = latestRatings; }
}
