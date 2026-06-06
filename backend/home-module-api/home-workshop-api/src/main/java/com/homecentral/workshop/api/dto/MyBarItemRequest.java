package com.homecentral.workshop.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

@Schema(description = "新增/更新酒柜条目 (独立表,不混冰箱)")
public class MyBarItemRequest {

    @Schema(description = "原料 id", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long ingredientId;

    @Schema(description = "瓶身容量 ml (留空取系统默认值)", example = "750.0")
    @DecimalMin("0.0")
    private BigDecimal bottleCapacityMl;

    @Schema(description = "当前余量 ml (新增时若不填, 默认 = bottleCapacityMl)", example = "750.0")
    @PositiveOrZero
    private BigDecimal remainingMl;

    @Schema(description = "备注 (如 '灰雁, 朋友送的')", example = "灰雁, 朋友送的")
    private String notes;

    public Long getIngredientId() { return ingredientId; }
    public void setIngredientId(Long ingredientId) { this.ingredientId = ingredientId; }
    public BigDecimal getBottleCapacityMl() { return bottleCapacityMl; }
    public void setBottleCapacityMl(BigDecimal bottleCapacityMl) { this.bottleCapacityMl = bottleCapacityMl; }
    public BigDecimal getRemainingMl() { return remainingMl; }
    public void setRemainingMl(BigDecimal remainingMl) { this.remainingMl = remainingMl; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
