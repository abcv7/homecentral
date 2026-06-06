package com.homecentral.fridge.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "采购历史响应")
public class FridgeShoppingHistoryVO {

    @Schema(description = "批次 ID（同一确认购买的所有条目共享）", example = "550e8400-e29b-41d4-a716-446655440000")
    private String batchId;

    @Schema(description = "购买时间")
    private OffsetDateTime purchasedAt;

    @Schema(description = "对方 email")
    private String partnerEmail;

    @Schema(description = "是否已发邮件")
    private Boolean emailSent;

    @Schema(description = "本批次条目")
    private List<Item> items;

    public static class Item {
        private Long id;
        private String name;
        private Long categoryId;
        private BigDecimal quantity;
        private String unit;
        private String source;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
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

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public OffsetDateTime getPurchasedAt() { return purchasedAt; }
    public void setPurchasedAt(OffsetDateTime purchasedAt) { this.purchasedAt = purchasedAt; }
    public String getPartnerEmail() { return partnerEmail; }
    public void setPartnerEmail(String partnerEmail) { this.partnerEmail = partnerEmail; }
    public Boolean getEmailSent() { return emailSent; }
    public void setEmailSent(Boolean emailSent) { this.emailSent = emailSent; }
    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }
}
