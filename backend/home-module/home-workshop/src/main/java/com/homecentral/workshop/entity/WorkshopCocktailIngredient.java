package com.homecentral.workshop.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

@TableName("workshop_cocktail_ingredient")
public class WorkshopCocktailIngredient implements Serializable {

    private Long cocktailId;
    private Long ingredientId;
    private Boolean isMain;
    private java.math.BigDecimal plannedAmountMl;
    private Integer sortOrder;

    public Long getCocktailId() { return cocktailId; }
    public void setCocktailId(Long cocktailId) { this.cocktailId = cocktailId; }
    public Long getIngredientId() { return ingredientId; }
    public void setIngredientId(Long ingredientId) { this.ingredientId = ingredientId; }
    public Boolean getIsMain() { return isMain; }
    public void setIsMain(Boolean isMain) { this.isMain = isMain; }
    public java.math.BigDecimal getPlannedAmountMl() { return plannedAmountMl; }
    public void setPlannedAmountMl(java.math.BigDecimal plannedAmountMl) { this.plannedAmountMl = plannedAmountMl; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
