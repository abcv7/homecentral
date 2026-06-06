package com.homecentral.fridge.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "采购条目（确认购买时的单个食材）")
public class FridgeShoppingItem {

    @Schema(description = "食材名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "鸡蛋")
    @NotBlank
    @Size(max = 100)
    private String name;

    @Schema(description = "分类 ID（可选）", example = "5")
    private Long categoryId;

    @Schema(description = "数量（默认 1）", example = "2")
    @NotNull
    @DecimalMin(value = "0.01", message = "数量必须大于 0")
    private BigDecimal quantity;

    @Schema(description = "单位", example = "个")
    private String unit;

    @Schema(description = "数据来源（MANUAL/AI）", example = "MANUAL")
    private String source;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
