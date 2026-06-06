package com.homecentral.workshop.api.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "站内通知类型")
public enum NotificationType {

    @Schema(description = "点单创建 — 通知 maker")
    ORDER_CREATED,

    @Schema(description = "催单 — 通知 maker")
    URGE,

    @Schema(description = "接单 — 通知 requester")
    ACCEPTED,

    @Schema(description = "拒绝 — 通知 requester")
    DECLINED,

    @Schema(description = "开始做 — 通知 requester")
    MAKING,

    @Schema(description = "做好 — 通知 requester (含自动扣减酒柜余量提示)")
    READY,

    @Schema(description = "已评价 — 通知 maker")
    RATED,

    @Schema(description = "取消 — 通知对方")
    CANCELLED
}
