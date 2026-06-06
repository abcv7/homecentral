package com.homecentral.workshop.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "取消订单请求 (仅 requester 可调用)")
public class CancelOrderRequest {

    @Schema(description = "取消原因 (选填)", example = "突然不想喝了")
    @Size(max = 200)
    private String reason;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
