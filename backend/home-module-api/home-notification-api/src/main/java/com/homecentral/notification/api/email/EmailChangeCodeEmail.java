package com.homecentral.notification.api.email;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("email_change_code")
public class EmailChangeCodeEmail extends VerificationCodeEmail {

    public EmailChangeCodeEmail() {
    }

    public EmailChangeCodeEmail(String to, String code, int minutes, String email) {
        super(to, code, minutes, email, "邮箱变更", "更换绑定邮箱");
    }

    @Override
    public String getDefaultSubject() {
        return "【栖物集】您正在更换绑定邮箱";
    }
}
