package com.homecentral.fridge.api.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "食材数据来源")
public enum FridgeItemSource {
    @Schema(description = "手动录入")
    MANUAL,
    @Schema(description = "拍照 AI 识别")
    AI
}
