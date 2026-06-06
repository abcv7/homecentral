package com.homecentral.workshop.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "鸡尾酒详情 VO")
public class CocktailVO {

    private Long id;
    private String nameZh;
    private String nameEn;
    private List<String> nameAlias;
    private String cover;
    private Integer views;
    private String recipeZh;
    private String methodZh;
    private String aroma;
    private String taste;
    private String style;
    private String scene;
    private String history;
    private String sourceUrl;
    private OffsetDateTime lastSyncedAt;
    private List<IngredientVO> ingredients;
    private BigDecimal ratingCount;
    private BigDecimal tasteAvg;
    private BigDecimal appearanceAvg;
    private BigDecimal overallAvg;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNameZh() { return nameZh; }
    public void setNameZh(String nameZh) { this.nameZh = nameZh; }
    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }
    public List<String> getNameAlias() { return nameAlias; }
    public void setNameAlias(List<String> nameAlias) { this.nameAlias = nameAlias; }
    public String getCover() { return cover; }
    public void setCover(String cover) { this.cover = cover; }
    public Integer getViews() { return views; }
    public void setViews(Integer views) { this.views = views; }
    public String getRecipeZh() { return recipeZh; }
    public void setRecipeZh(String recipeZh) { this.recipeZh = recipeZh; }
    public String getMethodZh() { return methodZh; }
    public void setMethodZh(String methodZh) { this.methodZh = methodZh; }
    public String getAroma() { return aroma; }
    public void setAroma(String aroma) { this.aroma = aroma; }
    public String getTaste() { return taste; }
    public void setTaste(String taste) { this.taste = taste; }
    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }
    public String getScene() { return scene; }
    public void setScene(String scene) { this.scene = scene; }
    public String getHistory() { return history; }
    public void setHistory(String history) { this.history = history; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    public OffsetDateTime getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(OffsetDateTime lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }
    public List<IngredientVO> getIngredients() { return ingredients; }
    public void setIngredients(List<IngredientVO> ingredients) { this.ingredients = ingredients; }
    public BigDecimal getRatingCount() { return ratingCount; }
    public void setRatingCount(BigDecimal ratingCount) { this.ratingCount = ratingCount; }
    public BigDecimal getTasteAvg() { return tasteAvg; }
    public void setTasteAvg(BigDecimal tasteAvg) { this.tasteAvg = tasteAvg; }
    public BigDecimal getAppearanceAvg() { return appearanceAvg; }
    public void setAppearanceAvg(BigDecimal appearanceAvg) { this.appearanceAvg = appearanceAvg; }
    public BigDecimal getOverallAvg() { return overallAvg; }
    public void setOverallAvg(BigDecimal overallAvg) { this.overallAvg = overallAvg; }
}
