package com.homecentral.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class SendCodeRequest {

    @Schema(description = "用途：EMAIL_CHANGE | PASSWORD_CHANGE")
    @NotBlank
    @Pattern(regexp = "EMAIL_CHANGE|PASSWORD_CHANGE", message = "purpose 必须是 EMAIL_CHANGE 或 PASSWORD_CHANGE")
    private String purpose;

    @Schema(description = "目标邮箱（EMAIL_CHANGE 时 = 新邮箱；PASSWORD_CHANGE 时可省略，从当前用户邮箱取）")
    @Email
    private String email;

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
