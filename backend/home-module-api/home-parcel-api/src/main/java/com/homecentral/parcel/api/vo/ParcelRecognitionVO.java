package com.homecentral.parcel.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.Map;

@Schema(description = "单个快递识别结果")
public class ParcelRecognitionVO {

    @Schema(description = "快递公司（标准化名称）")
    private String courierCompany;

    @Schema(description = "运单号")
    private String trackingNumber;

    @Schema(description = "取件码")
    private String pickupCode;

    @Schema(description = "归属人/收件人")
    private String ownerName;

    @Schema(description = "到站日期")
    private LocalDate arrivedDate;

    @Schema(description = "商品名称（AI识别，无法识别填未知）")
    private String productName;

    @Schema(description = "总体置信度: HIGH/MEDIUM/LOW")
    private String confidence;

    @Schema(description = "各字段置信度")
    private Map<String, String> fieldConfidence;

    public String getCourierCompany() { return courierCompany; }
    public void setCourierCompany(String courierCompany) { this.courierCompany = courierCompany; }
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    public String getPickupCode() { return pickupCode; }
    public void setPickupCode(String pickupCode) { this.pickupCode = pickupCode; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public LocalDate getArrivedDate() { return arrivedDate; }
    public void setArrivedDate(LocalDate arrivedDate) { this.arrivedDate = arrivedDate; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getConfidence() { return confidence; }
    public void setConfidence(String confidence) { this.confidence = confidence; }
    public Map<String, String> getFieldConfidence() { return fieldConfidence; }
    public void setFieldConfidence(Map<String, String> fieldConfidence) { this.fieldConfidence = fieldConfidence; }
}
