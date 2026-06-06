package com.homecentral.workshop.api.vo;

import com.homecentral.workshop.api.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "评分详情 VO")
public class OrderRatingVO {

    private Long id;
    private Long orderId;
    private Long userId;
    private String userNickname;
    private Short tasteScore;
    private Short appearanceScore;
    private BigDecimal overallScore;
    private String comment;
    private List<String> photoFileIds;
    private List<String> photoUrls;
    private java.time.OffsetDateTime ratedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserNickname() { return userNickname; }
    public void setUserNickname(String userNickname) { this.userNickname = userNickname; }
    public Short getTasteScore() { return tasteScore; }
    public void setTasteScore(Short tasteScore) { this.tasteScore = tasteScore; }
    public Short getAppearanceScore() { return appearanceScore; }
    public void setAppearanceScore(Short appearanceScore) { this.appearanceScore = appearanceScore; }
    public BigDecimal getOverallScore() { return overallScore; }
    public void setOverallScore(BigDecimal overallScore) { this.overallScore = overallScore; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public List<String> getPhotoFileIds() { return photoFileIds; }
    public void setPhotoFileIds(List<String> photoFileIds) { this.photoFileIds = photoFileIds; }
    public List<String> getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(List<String> photoUrls) { this.photoUrls = photoUrls; }
    public java.time.OffsetDateTime getRatedAt() { return ratedAt; }
    public void setRatedAt(java.time.OffsetDateTime ratedAt) { this.ratedAt = ratedAt; }
}
