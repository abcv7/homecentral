package com.homecentral.notification.api.email;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.HashMap;
import java.util.Map;

@JsonTypeName("friend_accept")
public class FriendAcceptEmail extends EmailMessage {

    private String groupName;
    private String inviterNickname;
    private String accepterNickname;
    private String accepterUsername;

    public FriendAcceptEmail() {
    }

    public FriendAcceptEmail(String to, String groupName, String inviterNickname,
                             String accepterNickname, String accepterUsername) {
        super(to);
        this.groupName = groupName;
        this.inviterNickname = inviterNickname;
        this.accepterNickname = accepterNickname;
        this.accepterUsername = accepterUsername;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getInviterNickname() {
        return inviterNickname;
    }

    public void setInviterNickname(String inviterNickname) {
        this.inviterNickname = inviterNickname;
    }

    public String getAccepterNickname() {
        return accepterNickname;
    }

    public void setAccepterNickname(String accepterNickname) {
        this.accepterNickname = accepterNickname;
    }

    public String getAccepterUsername() {
        return accepterUsername;
    }

    public void setAccepterUsername(String accepterUsername) {
        this.accepterUsername = accepterUsername;
    }

    @Override
    public String getDefaultSubject() {
        return "【栖物集】好友接受了您的邀请";
    }

    @Override
    public String getTemplatePath() {
        return "email/notification.html";
    }

    @Override
    public Map<String, Object> toVariables() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("badge", "好友关系");
        vars.put("headline", "好友邀请已被接受");
        vars.put("greeting", "Hi " + safe(inviterNickname) + "，");
        vars.put("body", "<b>" + safe(accepterNickname) + "</b>（" + safe(accepterUsername) + "）"
            + " 已接受您在分组 <b>「" + safe(groupName) + "」</b> 发起的好友邀请，"
            + "现在你们可以互相查看快递、共享购物清单。");
        vars.put("hint", "您可以返回栖物集，在「好友」页面管理分组与可见性。");
        vars.put("year", EmailRenderSupport.currentYear());
        return vars;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
