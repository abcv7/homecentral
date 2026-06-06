package com.homecentral.workshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.springframework.data.annotation.Transient;

import java.time.OffsetDateTime;

@TableName("workshop_cocktail")
public class WorkshopCocktail {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String nameZh;
    private String nameEn;

    @TableField(typeHandler = com.homecentral.workshop.config.StringArrayTypeHandler.class)
    private String[] nameAlias;

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
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @Transient
    @TableField(exist = false)
    private java.util.List<Long> ingredientIds;

    @Transient
    @TableField(exist = false)
    private java.util.List<Long> mainIngredientIds;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNameZh() { return nameZh; }
    public void setNameZh(String nameZh) { this.nameZh = nameZh; }
    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }
    public String[] getNameAlias() { return nameAlias; }
    public void setNameAlias(String[] nameAlias) { this.nameAlias = nameAlias; }
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
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
    public java.util.List<Long> getIngredientIds() { return ingredientIds; }
    public void setIngredientIds(java.util.List<Long> ingredientIds) { this.ingredientIds = ingredientIds; }
    public java.util.List<Long> getMainIngredientIds() { return mainIngredientIds; }
    public void setMainIngredientIds(java.util.List<Long> mainIngredientIds) { this.mainIngredientIds = mainIngredientIds; }
}
