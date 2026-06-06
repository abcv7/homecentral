package com.homecentral.workshop.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "调整某项原料的实际用量 (男生在 ACCEPTED/MAKING 阶段可改)")
public class OrderItemAdjustRequest {

    @Schema(description = "原料 id", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long ingredientId;

    @Schema(description = "实际用量 (ml)", example = "45.0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @Positive
    private BigDecimal actualAmountMl;

    public Long getIngredientId() { return ingredientId; }
    public void setIngredientId(Long ingredientId) { this.ingredientId = ingredientId; }
    public BigDecimal getActualAmountMl() { return actualAmountMl; }
    public void setActualAmountMl(BigDecimal actualAmountMl) { this.actualAmountMl = actualAmountMl; }
}
