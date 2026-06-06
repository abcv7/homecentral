package com.homecentral.parcel.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@TableName(value = "parcel", schema = "home_parcel")
public class Parcel {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String courierCompany;
    private String trackingNumber;
    private String pickupCode;
    private String status;
    private String remark;
    private String attachmentUrl;
    private Long createdBy;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @TableLogic
    private Boolean deleted;

    private String source;
    private String ownerName;
    private Long ownerUserId;
    private LocalDate arrivedDate;
    private Integer daysAtStation;
    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private String apiTrackingRaw;
    private String productName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCourierCompany() { return courierCompany; }
    public void setCourierCompany(String courierCompany) { this.courierCompany = courierCompany; }
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    public String getPickupCode() { return pickupCode; }
    public void setPickupCode(String pickupCode) { this.pickupCode = pickupCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }
    public LocalDate getArrivedDate() { return arrivedDate; }
    public void setArrivedDate(LocalDate arrivedDate) { this.arrivedDate = arrivedDate; }
    public Integer getDaysAtStation() { return daysAtStation; }
    public void setDaysAtStation(Integer daysAtStation) { this.daysAtStation = daysAtStation; }
    public String getApiTrackingRaw() { return apiTrackingRaw; }
    public void setApiTrackingRaw(String apiTrackingRaw) { this.apiTrackingRaw = apiTrackingRaw; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
}
