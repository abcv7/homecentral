package com.homecentral.workshop.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "订单原料条目 VO (planned + actual)")
public class OrderItemVO {

    private Long id;
    private Long ingredientId;
    private String ingredientNameZh;
    private String ingredientNameEn;
    private BigDecimal plannedAmountMl;
    private BigDecimal actualAmountMl;
    private Boolean isMain;
    private Boolean deductionApplied;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIngredientId() { return ingredientId; }
    public void setIngredientId(Long ingredientId) { this.ingredientId = ingredientId; }
    public String getIngredientNameZh() { return ingredientNameZh; }
    public void setIngredientNameZh(String ingredientNameZh) { this.ingredientNameZh = ingredientNameZh; }
    public String getIngredientNameEn() { return ingredientNameEn; }
    public void setIngredientNameEn(String ingredientNameEn) { this.ingredientNameEn = ingredientNameEn; }
    public BigDecimal getPlannedAmountMl() { return plannedAmountMl; }
    public void setPlannedAmountMl(BigDecimal plannedAmountMl) { this.plannedAmountMl = plannedAmountMl; }
    public BigDecimal getActualAmountMl() { return actualAmountMl; }
    public void setActualAmountMl(BigDecimal actualAmountMl) { this.actualAmountMl = actualAmountMl; }
    public Boolean getIsMain() { return isMain; }
    public void setIsMain(Boolean isMain) { this.isMain = isMain; }
    public Boolean getDeductionApplied() { return deductionApplied; }
    public void setDeductionApplied(Boolean deductionApplied) { this.deductionApplied = deductionApplied; }
}
