package com.homecentral.life.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "购物清单视图")
public class ShoppingMemoVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "物品名称")
    private String itemName;

    @Schema(description = "备注")
    private String note;

    @Schema(description = "是否已购买")
    private boolean purchased;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isPurchased() {
        return purchased;
    }

    public void setPurchased(boolean purchased) {
        this.purchased = purchased;
    }
}
