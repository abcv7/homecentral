package com.homecentral.workshop.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "催单请求 (rate-limited: 同一订单同一人 1min 内只发 1 次, 全程最多 3 次)")
public class UrgeOrderRequest {

    @Schema(description = "催单留言 (选填)", example = "快点啦, 渴死了!")
    @Size(max = 200)
    private String message;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
