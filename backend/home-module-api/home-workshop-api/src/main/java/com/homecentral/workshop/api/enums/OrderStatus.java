package com.homecentral.workshop.api.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "鸡尾酒/点酒工作流状态")
public enum OrderStatus {

    @Schema(description = "待接单 — 女生刚创建,等待男生点接受")
    PENDING,

    @Schema(description = "已接单 — 男生接受,等待点击'开始做'")
    ACCEPTED,

    @Schema(description = "制作中 — 男生点击'开始做',可边做边改用量")
    MAKING,

    @Schema(description = "已做好 — 男生标记完成,系统自动扣减酒柜余量 + 邮件通知 + 通知女生")
    READY,

    @Schema(description = "已评价 — 女生已打分 (永久可评)")
    RATED,

    @Schema(description = "已拒绝 — 男生拒绝接单 (PENDING 状态可触发)")
    DECLINED,

    @Schema(description = "已取消 — 女生主动撤单 (PENDING / ACCEPTED / MAKING 状态可触发)")
    CANCELLED
}
