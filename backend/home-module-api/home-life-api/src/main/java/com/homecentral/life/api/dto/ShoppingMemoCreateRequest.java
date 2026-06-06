package com.homecentral.life.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "创建购物清单请求")
public class ShoppingMemoCreateRequest {

    @Schema(description = "物品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "牛奶")
    @NotBlank
    private String itemName;

    @Schema(description = "备注", example = "全脂牛奶")
    private String note;

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
}
