package com.homecentral.workshop.api.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "订单日志动作类型 (status_change 之外的边路动作)")
public enum LogActionType {

    @Schema(description = "状态机流转 — 正常 accept/decline/making/ready/rate/cancel")
    STATUS,

    @Schema(description = "催单 — 女生催促男生 (rate-limited 1min/次, 最多 3 次)")
    URGE,

    @Schema(description = "调量 — 男生调整某项原料用量 (READY 之前都可改)")
    ADJUST,

    @Schema(description = "评价提交")
    RATE
}
