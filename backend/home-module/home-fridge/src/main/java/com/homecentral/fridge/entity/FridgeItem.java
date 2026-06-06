package com.homecentral.fridge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.homecentral.fridge.api.enums.FridgeItemSource;
import com.homecentral.fridge.api.enums.FridgeItemStatus;
import com.homecentral.fridge.api.enums.FridgeZone;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@TableName(value = "fridge_item", schema = "home_fridge")
public class FridgeItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private FridgeZone zone;
    private String subZone;
    private Long categoryId;
    private BigDecimal quantity;
    private String unit;
    private LocalDate purchaseDate;
    private LocalDate expiryDate;
    private String imageUrl;
    private FridgeItemSource source;
    private FridgeItemStatus status;
    private String aiConfidence;
    private String aiRaw;
    private String notes;
    private OffsetDateTime consumedAt;
    private Long createdBy;
    private OffsetDateTime createdAt;
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
    public FridgeItemSource getSource() { return source; }
    public void setSource(FridgeItemSource source) { this.source = source; }
    public FridgeItemStatus getStatus() { return status; }
    public void setStatus(FridgeItemStatus status) { this.status = status; }
    public String getAiConfidence() { return aiConfidence; }
    public void setAiConfidence(String aiConfidence) { this.aiConfidence = aiConfidence; }
    public String getAiRaw() { return aiRaw; }
    public void setAiRaw(String aiRaw) { this.aiRaw = aiRaw; }
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
