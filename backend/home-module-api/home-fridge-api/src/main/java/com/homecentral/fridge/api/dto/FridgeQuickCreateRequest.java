package com.homecentral.fridge.api.dto;

import com.homecentral.fridge.api.enums.FridgeZone;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "快速新增食材请求（采购篮入库专用，字段精简）")
public class FridgeQuickCreateRequest {

    @Schema(description = "食材名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "鸡蛋")
    @NotBlank
    @Size(max = 100)
    private String name;

    @Schema(description = "分类 ID（可选）", example = "5")
    private Long categoryId;

    @Schema(description = "数量（默认 1，必须 > 0）", example = "3")
    @DecimalMin(value = "0.01", message = "数量必须大于 0")
    private BigDecimal quantity;

    @Schema(description = "购买日期（默认今天）", example = "2026-06-01")
    private String purchaseDate;

    @Schema(description = "过期日期", example = "2026-06-15")
    private String expiryDate;

    @Schema(description = "目标区域（默认 REFRIGERATED）；选择 null/PENDING 表示先进采购篮", example = "REFRIGERATED")
    private FridgeZone zone;

    @Schema(description = "可选子区域（层/门），例如 REFRIGERATED-L1 / DOOR-LEFT-L1", example = "REFRIGERATED-L1")
    private String subZone;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(String purchaseDate) { this.purchaseDate = purchaseDate; }
    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    public FridgeZone getZone() { return zone; }
    public void setZone(FridgeZone zone) { this.zone = zone; }
    public String getSubZone() { return subZone; }
    public void setSubZone(String subZone) { this.subZone = subZone; }
}
