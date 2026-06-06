package com.homecentral.fridge.api.vo;

import com.homecentral.fridge.api.enums.FridgeZone;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Schema(description = "单条食材识别结果（供前端预填表单）")
public class FridgeRecognizeVO {

    @Schema(description = "食材名称（AI 识别）")
    private String name;

    @Schema(description = "推荐区域")
    private FridgeZone suggestedZone;

    @Schema(description = "推荐子区域")
    private String suggestedSubZone;

    @Schema(description = "推荐分类 ID（匹配系统预置）")
    private Long suggestedCategoryId;

    @Schema(description = "推荐分类名（无法匹配时返回原始建议）")
    private String suggestedCategoryName;

    @Schema(description = "推荐数量")
    private BigDecimal estimatedQuantity;

    @Schema(description = "推荐单位")
    private String estimatedUnit;

    @Schema(description = "推荐购买日期（默认今天）")
    private LocalDate purchaseDate;

    @Schema(description = "推荐过期日期（AI 估算）")
    private LocalDate expiryDate;

    @Schema(description = "AI 备注，例如识别依据")
    private String notes;

    @Schema(description = "总体置信度 HIGH/MEDIUM/LOW")
    private String confidence;

    @Schema(description = "各字段置信度")
    private Map<String, String> fieldConfidence;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public FridgeZone getSuggestedZone() { return suggestedZone; }
    public void setSuggestedZone(FridgeZone suggestedZone) { this.suggestedZone = suggestedZone; }
    public String getSuggestedSubZone() { return suggestedSubZone; }
    public void setSuggestedSubZone(String suggestedSubZone) { this.suggestedSubZone = suggestedSubZone; }
    public Long getSuggestedCategoryId() { return suggestedCategoryId; }
    public void setSuggestedCategoryId(Long suggestedCategoryId) { this.suggestedCategoryId = suggestedCategoryId; }
    public String getSuggestedCategoryName() { return suggestedCategoryName; }
    public void setSuggestedCategoryName(String suggestedCategoryName) { this.suggestedCategoryName = suggestedCategoryName; }
    public BigDecimal getEstimatedQuantity() { return estimatedQuantity; }
    public void setEstimatedQuantity(BigDecimal estimatedQuantity) { this.estimatedQuantity = estimatedQuantity; }
    public String getEstimatedUnit() { return estimatedUnit; }
    public void setEstimatedUnit(String estimatedUnit) { this.estimatedUnit = estimatedUnit; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getConfidence() { return confidence; }
    public void setConfidence(String confidence) { this.confidence = confidence; }
    public Map<String, String> getFieldConfidence() { return fieldConfidence; }
    public void setFieldConfidence(Map<String, String> fieldConfidence) { this.fieldConfidence = fieldConfidence; }
}
