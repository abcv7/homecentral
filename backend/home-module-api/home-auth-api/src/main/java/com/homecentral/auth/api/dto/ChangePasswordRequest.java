package com.homecentral.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequest {

    @Schema(description = "6 位验证码（已发到当前邮箱）")
    @NotBlank
    @Size(min = 6, max = 6, message = "验证码必须是 6 位")
    private String code;

    @Schema(description = "新密码（6-20 位）")
    @NotBlank
    @Size(min = 6, max = 20, message = "密码长度必须 6-20 位")
    private String newPassword;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
