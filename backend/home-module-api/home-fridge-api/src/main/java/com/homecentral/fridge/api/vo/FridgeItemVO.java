package com.homecentral.fridge.api.vo;

import com.homecentral.fridge.api.enums.FridgeItemSource;
import com.homecentral.fridge.api.enums.FridgeItemStatus;
import com.homecentral.fridge.api.enums.FridgeZone;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Schema(description = "食材视图")
public class FridgeItemVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "食材名称")
    private String name;

    @Schema(description = "区域")
    private FridgeZone zone;

    @Schema(description = "子区域")
    private String subZone;

    @Schema(description = "分类 ID")
    private Long categoryId;

    @Schema(description = "分类名称（联表回填）")
    private String categoryName;

    @Schema(description = "分类图标")
    private String categoryIcon;

    @Schema(description = "分类颜色")
    private String categoryColor;

    @Schema(description = "数量")
    private BigDecimal quantity;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "购买日期")
    private LocalDate purchaseDate;

    @Schema(description = "过期日期")
    private LocalDate expiryDate;

    @Schema(description = "距过期天数（负数=已过期）")
    private Long daysToExpiry;

    @Schema(description = "图片 URL")
    private String imageUrl;

    @Schema(description = "数据来源")
    private FridgeItemSource source;

    @Schema(description = "状态")
    private FridgeItemStatus status;

    @Schema(description = "备注")
    private String notes;

    @Schema(description = "消耗时间")
    private OffsetDateTime consumedAt;

    @Schema(description = "创建人")
    private Long createdBy;

    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;

    @Schema(description = "更新时间")
    private OffsetDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public FridgeZone getZone() { return zone; }
    public void setZone(FridgeZone zone) { this.zone = zone; }
    public String getSubZone() { return subZone; }
    public void setSubZone(String subZone) { this.subZone = subZone; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getCategoryIcon() { return categoryIcon; }
    public void setCategoryIcon(String categoryIcon) { this.categoryIcon = categoryIcon; }
    public String getCategoryColor() { return categoryColor; }
    public void setCategoryColor(String categoryColor) { this.categoryColor = categoryColor; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public Long getDaysToExpiry() { return daysToExpiry; }
    public void setDaysToExpiry(Long daysToExpiry) { this.daysToExpiry = daysToExpiry; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public FridgeItemSource getSource() { return source; }
    public void setSource(FridgeItemSource source) { this.source = source; }
    public FridgeItemStatus getStatus() { return status; }
    public void setStatus(FridgeItemStatus status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public OffsetDateTime getConsumedAt() { return consumedAt; }
    public void setConsumedAt(OffsetDateTime consumedAt) { this.consumedAt = consumedAt; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
