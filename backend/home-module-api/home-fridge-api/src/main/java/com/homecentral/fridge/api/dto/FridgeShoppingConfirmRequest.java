package com.homecentral.fridge.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "采购确认请求（一次确认 = 一个 batch，携若干条目）")
public class FridgeShoppingConfirmRequest {

    @Schema(description = "采购条目列表（可多件）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    @Valid
    private List<FridgeShoppingItem> items;

    @Schema(description = "对方 email（可选，填写时同步发邮件通知）", example = "partner@example.com")
    private String partnerEmail;

    @Schema(description = "组内成员所在分组 ID（可选，填写时把采购清单同步给该组所有 ACCEPTED 成员）")
    private Long groupId;

    @Schema(description = "采购篮中要一并清空的 fridge_item ID 列表（仅 PENDING 且属于当前用户；空数组表示不清空）")
    private List<Long> basketItemIds;

    public List<FridgeShoppingItem> getItems() { return items; }
    public void setItems(List<FridgeShoppingItem> items) { this.items = items; }
    public String getPartnerEmail() { return partnerEmail; }
    public void setPartnerEmail(String partnerEmail) { this.partnerEmail = partnerEmail; }
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public List<Long> getBasketItemIds() { return basketItemIds; }
    public void setBasketItemIds(List<Long> basketItemIds) { this.basketItemIds = basketItemIds; }
}
