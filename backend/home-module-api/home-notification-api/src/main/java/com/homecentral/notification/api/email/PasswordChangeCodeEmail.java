package com.homecentral.notification.api.email;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("password_change_code")
public class PasswordChangeCodeEmail extends VerificationCodeEmail {

    public PasswordChangeCodeEmail() {
    }

    public PasswordChangeCodeEmail(String to, String code, int minutes, String email) {
        super(to, code, minutes, email, "密码变更", "修改登录密码");
    }

    @Override
    public String getDefaultSubject() {
        return "【栖物集】您正在修改登录密码";
    }
}
