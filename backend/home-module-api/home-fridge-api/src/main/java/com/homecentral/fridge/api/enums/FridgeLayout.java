package com.homecentral.fridge.api.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "冰箱门型/布局")
public enum FridgeLayout {
    @Schema(description = "经典单开：上冷冻 + 下冷藏")
    CLASSIC,
    @Schema(description = "冷冻在下：上冷藏 + 下冷冻")
    BOTTOM_FREEZER,
    @Schema(description = "对开门：左冷冻 + 右冷藏")
    SIDE_BY_SIDE,
    @Schema(description = "法式三门：冷藏 + 解冻 + 冷冻上下三段，左右双门")
    THREE_DOOR
}
