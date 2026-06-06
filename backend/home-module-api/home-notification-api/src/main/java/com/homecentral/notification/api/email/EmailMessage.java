package com.homecentral.notification.api.email;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.HashMap;
import java.util.Map;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = EmailChangeCodeEmail.class, name = "email_change_code"),
    @JsonSubTypes.Type(value = PasswordChangeCodeEmail.class, name = "password_change_code"),
    @JsonSubTypes.Type(value = FriendAcceptEmail.class, name = "friend_accept"),
    @JsonSubTypes.Type(value = PurchaseNoticeEmail.class, name = "purchase_notice")
})
public abstract class EmailMessage {

    private String to;

    public EmailMessage() {
    }

    public EmailMessage(String to) {
        this.to = to;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    @JsonIgnore
    public abstract String getDefaultSubject();

    @JsonIgnore
    public abstract String getTemplatePath();

    @JsonIgnore
    public Map<String, Object> toVariables() {
        return new HashMap<>();
    }
}
