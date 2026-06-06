package com.homecentral.fridge.api.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "食材状态")
public enum FridgeItemStatus {
    @Schema(description = "采购篮：已添加/拍照识别但未归位，3D 视图中可拖拽到具体层")
    PENDING,
    @Schema(description = "在库：已归位到具体温区 + 层")
    ACTIVE,
    @Schema(description = "已消耗")
    CONSUMED,
    @Schema(description = "已丢弃")
    DISCARDED
}
