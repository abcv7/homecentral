package com.homecentral.notification.api.email;

import java.util.HashMap;
import java.util.Map;

public abstract class VerificationCodeEmail extends EmailMessage {

    private String code;
    private int minutes;
    private String email;
    private String purposeText;
    private String purposeBadge;

    public VerificationCodeEmail() {
    }

    public VerificationCodeEmail(String to, String code, int minutes, String email,
                                 String purposeText, String purposeBadge) {
        super(to);
        this.code = code;
        this.minutes = minutes;
        this.email = email;
        this.purposeText = purposeText;
        this.purposeBadge = purposeBadge;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPurposeText() {
        return purposeText;
    }

    public void setPurposeText(String purposeText) {
        this.purposeText = purposeText;
    }

    public String getPurposeBadge() {
        return purposeBadge;
    }

    public void setPurposeBadge(String purposeBadge) {
        this.purposeBadge = purposeBadge;
    }

    @Override
    public String getTemplatePath() {
        return "email/verification-code.html";
    }

    @Override
    public Map<String, Object> toVariables() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("code", code);
        vars.put("codeBoxes", EmailRenderSupport.renderCodeBoxes(code));
        vars.put("minutes", minutes);
        vars.put("email", email);
        vars.put("purposeText", purposeText);
        vars.put("purposeBadge", purposeBadge);
        vars.put("year", EmailRenderSupport.currentYear());
        return vars;
    }
}
