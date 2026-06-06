package com.homecentral.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class VerifyEmailCodeRequest {

    @Schema(description = "用途：EMAIL_CHANGE | PASSWORD_CHANGE")
    @NotBlank
    @Pattern(regexp = "EMAIL_CHANGE|PASSWORD_CHANGE", message = "purpose 必须是 EMAIL_CHANGE 或 PASSWORD_CHANGE")
    private String purpose;

    @Schema(description = "邮箱（与发送验证码时一致）")
    @NotBlank
    @Email
    private String email;

    @Schema(description = "6 位验证码")
    @NotBlank
    @Size(min = 6, max = 6, message = "验证码必须是 6 位")
    private String code;

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
