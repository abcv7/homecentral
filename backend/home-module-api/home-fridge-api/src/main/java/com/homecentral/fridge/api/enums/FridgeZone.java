package com.homecentral.fridge.api.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "冰箱区域枚举")
public enum FridgeZone {
    @Schema(description = "冷藏区")
    REFRIGERATED,
    @Schema(description = "冷冻区")
    FROZEN
}
