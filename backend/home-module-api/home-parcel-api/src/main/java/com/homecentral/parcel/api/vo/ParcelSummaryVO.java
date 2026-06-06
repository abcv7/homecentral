package com.homecentral.parcel.api.vo;

import com.homecentral.parcel.api.enums.ParcelStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Schema(description = "快递摘要视图")
public class ParcelSummaryVO {

    @Schema(description = "快递ID")
    private Long id;

    @Schema(description = "快递公司")
    private String courierCompany;

    @Schema(description = "快递单号")
    private String trackingNumber;

    @Schema(description = "取件码")
    private String pickupCode;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "附件URL")
    private String attachmentUrl;

    @Schema(description = "状态")
    private ParcelStatus status;

    @Schema(description = "创建人ID")
    private Long createdBy;

    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;

    @Schema(description = "数据来源（MANUAL/API）")
    private String source;

    @Schema(description = "归属人")
    private String ownerName;

    @Schema(description = "到站天数")
    private Integer daysAtStation;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "已分享给的用户数")
    private Integer sharedCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCourierCompany() {
        return courierCompany;
    }

    public void setCourierCompany(String courierCompany) {
        this.courierCompany = courierCompany;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getPickupCode() {
        return pickupCode;
    }

    public void setPickupCode(String pickupCode) {
        this.pickupCode = pickupCode;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }

    public ParcelStatus getStatus() {
        return status;
    }

    public void setStatus(ParcelStatus status) {
        this.status = status;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public Integer getDaysAtStation() {
        return daysAtStation;
    }

    public void setDaysAtStation(Integer daysAtStation) {
        this.daysAtStation = daysAtStation;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getSharedCount() {
        return sharedCount;
    }

    public void setSharedCount(Integer sharedCount) {
        this.sharedCount = sharedCount;
    }
}
