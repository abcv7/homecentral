package com.homecentral.workshop.api.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "酒柜 (My Bar) 余量状态")
public enum BarStatus {

    @Schema(description = "在用 — 余量 > 0,可在推荐中显示")
    ACTIVE,

    @Schema(description = "已耗尽 — 余量 = 0,需要补货或丢弃")
    DEPLETED,

    @Schema(description = "已丢弃 — 用户主动丢弃,不再参与推荐")
    DISCARDED
}
