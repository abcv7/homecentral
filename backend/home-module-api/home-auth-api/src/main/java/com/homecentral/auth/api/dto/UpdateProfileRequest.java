package com.homecentral.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    @Schema(description = "昵称（1-32 字符）")
    @Size(max = 32, message = "昵称长度不能超过 32 字符")
    private String nickname;

    @Schema(description = "手机号（20 字符内）")
    @Size(max = 20, message = "手机号长度不能超过 20 字符")
    private String phone;

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
