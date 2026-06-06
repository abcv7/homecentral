package com.homecentral.parcel.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(description = "包裹已分享给的接收人信息")
public class SharedParcelUserVO {

    @Schema(description = "接收人用户ID")
    private Long userId;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "分享时间")
    private OffsetDateTime sharedAt;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public OffsetDateTime getSharedAt() {
        return sharedAt;
    }

    public void setSharedAt(OffsetDateTime sharedAt) {
        this.sharedAt = sharedAt;
    }
}
