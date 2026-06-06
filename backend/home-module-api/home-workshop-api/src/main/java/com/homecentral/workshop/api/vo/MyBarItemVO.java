package com.homecentral.workshop.api.vo;

import com.homecentral.workshop.api.enums.BarStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(description = "酒柜条目 (前端渲染酒瓶 UI 用)")
public class MyBarItemVO {

    private Long id;
    private Long userId;
    private IngredientVO ingredient;
    private BigDecimal bottleCapacityMl;
    private BigDecimal remainingMl;
    private BigDecimal percentage;
    private BarStatus status;
    private String notes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public IngredientVO getIngredient() { return ingredient; }
    public void setIngredient(IngredientVO ingredient) { this.ingredient = ingredient; }
    public BigDecimal getBottleCapacityMl() { return bottleCapacityMl; }
    public void setBottleCapacityMl(BigDecimal bottleCapacityMl) { this.bottleCapacityMl = bottleCapacityMl; }
    public BigDecimal getRemainingMl() { return remainingMl; }
    public void setRemainingMl(BigDecimal remainingMl) { this.remainingMl = remainingMl; }
    public BigDecimal getPercentage() { return percentage; }
    public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }
    public BarStatus getStatus() { return status; }
    public void setStatus(BarStatus status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
