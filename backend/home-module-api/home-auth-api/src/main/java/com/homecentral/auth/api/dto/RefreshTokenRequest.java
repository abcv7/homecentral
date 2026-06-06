package com.homecentral.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "刷新Token请求")
public class RefreshTokenRequest {

    @Schema(description = "刷新令牌", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "refreshToken不能为空")
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
