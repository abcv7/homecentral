package com.homecentral.workshop.api.vo;

import com.homecentral.workshop.api.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "点单详情 VO")
public class DrinkOrderVO {

    private Long id;
    private Long requesterId;
    private String requesterNickname;
    private Long makerId;
    private String makerNickname;
    private Long groupId;
    private String groupName;
    private Long cocktailId;
    private String cocktailNameZh;
    private String cocktailNameEn;
    private String cocktailCover;
    private String recipeZh;
    private String methodZh;
    private List<OrderItemVO> items;
    private OrderStatus status;
    private String message;
    private Integer urgeCount;
    private OffsetDateTime requestedAt;
    private OffsetDateTime respondedAt;
    private OffsetDateTime makingAt;
    private OffsetDateTime readyAt;
    private OffsetDateTime ratedAt;
    private String cancelReason;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRequesterId() { return requesterId; }
    public void setRequesterId(Long requesterId) { this.requesterId = requesterId; }
    public String getRequesterNickname() { return requesterNickname; }
    public void setRequesterNickname(String requesterNickname) { this.requesterNickname = requesterNickname; }
    public Long getMakerId() { return makerId; }
    public void setMakerId(Long makerId) { this.makerId = makerId; }
    public String getMakerNickname() { return makerNickname; }
    public void setMakerNickname(String makerNickname) { this.makerNickname = makerNickname; }
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public Long getCocktailId() { return cocktailId; }
    public void setCocktailId(Long cocktailId) { this.cocktailId = cocktailId; }
    public String getCocktailNameZh() { return cocktailNameZh; }
    public void setCocktailNameZh(String cocktailNameZh) { this.cocktailNameZh = cocktailNameZh; }
    public String getCocktailNameEn() { return cocktailNameEn; }
    public void setCocktailNameEn(String cocktailNameEn) { this.cocktailNameEn = cocktailNameEn; }
    public String getCocktailCover() { return cocktailCover; }
    public void setCocktailCover(String cocktailCover) { this.cocktailCover = cocktailCover; }
    public String getRecipeZh() { return recipeZh; }
    public void setRecipeZh(String recipeZh) { this.recipeZh = recipeZh; }
    public String getMethodZh() { return methodZh; }
    public void setMethodZh(String methodZh) { this.methodZh = methodZh; }
    public List<OrderItemVO> getItems() { return items; }
    public void setItems(List<OrderItemVO> items) { this.items = items; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Integer getUrgeCount() { return urgeCount; }
    public void setUrgeCount(Integer urgeCount) { this.urgeCount = urgeCount; }
    public OffsetDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(OffsetDateTime requestedAt) { this.requestedAt = requestedAt; }
    public OffsetDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(OffsetDateTime respondedAt) { this.respondedAt = respondedAt; }
    public OffsetDateTime getMakingAt() { return makingAt; }
    public void setMakingAt(OffsetDateTime makingAt) { this.makingAt = makingAt; }
    public OffsetDateTime getReadyAt() { return readyAt; }
    public void setReadyAt(OffsetDateTime readyAt) { this.readyAt = readyAt; }
    public OffsetDateTime getRatedAt() { return ratedAt; }
    public void setRatedAt(OffsetDateTime ratedAt) { this.ratedAt = ratedAt; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
