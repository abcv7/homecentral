package com.homecentral.fridge.api.dto;

import com.homecentral.fridge.api.enums.FridgeItemSource;
import com.homecentral.fridge.api.enums.FridgeZone;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "创建 / 更新食材请求")
public class FridgeItemRequest {

    @Schema(description = "食材名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "纯牛奶")
    @NotBlank
    private String name;

    @Schema(description = "区域", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "REFRIGERATED", allowableValues = {"REFRIGERATED", "FROZEN"})
    @NotNull
    private FridgeZone zone;

    @Schema(description = "子区域自由文本", example = "冷藏-中层")
    private String subZone;

    @Schema(description = "分类 ID（可选，关联 fridge_category.id）", example = "1")
    private Long categoryId;

    @Schema(description = "数量", example = "1")
    private BigDecimal quantity;

    @Schema(description = "单位", example = "瓶")
    private String unit;

    @Schema(description = "购买日期", example = "2026-06-01")
    private LocalDate purchaseDate;

    @Schema(description = "过期日期", example = "2026-06-15")
    private LocalDate expiryDate;

    @Schema(description = "食材图片 URL（已上传到 home-file 后的地址）", example = "https://minio/.../xxx.jpg")
    private String imageUrl;

    @Schema(description = "备注", example = "蒙牛纯牛奶")
    private String notes;

    @Schema(description = "数据来源", example = "MANUAL", allowableValues = {"MANUAL", "AI"})
    private FridgeItemSource source;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public FridgeZone getZone() { return zone; }
    public void setZone(FridgeZone zone) { this.zone = zone; }
    public String getSubZone() { return subZone; }
    public void setSubZone(String subZone) { this.subZone = subZone; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public FridgeItemSource getSource() { return source; }
    public void setSource(FridgeItemSource source) { this.source = source; }
}
