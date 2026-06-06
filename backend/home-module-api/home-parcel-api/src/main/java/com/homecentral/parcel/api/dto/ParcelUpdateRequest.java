package com.homecentral.parcel.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "更新快递请求")
public class ParcelUpdateRequest {

    @Schema(description = "快递公司", example = "顺丰速运")
    private String courierCompany;

    @Schema(description = "快递单号", example = "SF1234567890")
    private String trackingNumber;

    @Schema(description = "取件码", example = "1-2-3456")
    private String pickupCode;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "附件URL")
    private String attachmentUrl;

    @Schema(description = "归属人", example = "张三")
    private String ownerName;

    @Schema(description = "到站日期")
    private LocalDate arrivedDate;

    @Schema(description = "商品名称", example = "手机")
    private String productName;

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

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public LocalDate getArrivedDate() {
        return arrivedDate;
    }

    public void setArrivedDate(LocalDate arrivedDate) {
        this.arrivedDate = arrivedDate;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}
