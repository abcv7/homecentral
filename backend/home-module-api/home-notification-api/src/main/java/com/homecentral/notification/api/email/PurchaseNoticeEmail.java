package com.homecentral.notification.api.email;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonTypeName("purchase_notice")
public class PurchaseNoticeEmail extends EmailMessage {

    public enum Variant {
        PARTNER,
        GROUP
    }

    private Variant variant;
    private String recipientNickname;
    private String senderNickname;
    private String groupName;
    private List<PurchaseItem> items;

    public PurchaseNoticeEmail() {
    }

    public PurchaseNoticeEmail(String to, Variant variant, String recipientNickname,
                               String senderNickname, String groupName, List<PurchaseItem> items) {
        super(to);
        this.variant = variant == null ? Variant.PARTNER : variant;
        this.recipientNickname = recipientNickname;
        this.senderNickname = senderNickname;
        this.groupName = groupName;
        this.items = items;
    }

    public Variant getVariant() {
        return variant;
    }

    public void setVariant(Variant variant) {
        this.variant = variant;
    }

    public String getRecipientNickname() {
        return recipientNickname;
    }

    public void setRecipientNickname(String recipientNickname) {
        this.recipientNickname = recipientNickname;
    }

    public String getSenderNickname() {
        return senderNickname;
    }

    public void setSenderNickname(String senderNickname) {
        this.senderNickname = senderNickname;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<PurchaseItem> getItems() {
        return items;
    }

    public void setItems(List<PurchaseItem> items) {
        this.items = items;
    }

    @Override
    public String getDefaultSubject() {
        return "【栖物集】家庭采购清单已确认";
    }

    @Override
    public String getTemplatePath() {
        return "email/notification.html";
    }

    @Override
    public Map<String, Object> toVariables() {
        Map<String, Object> vars = new HashMap<>();
        boolean isGroup = variant == Variant.GROUP;
        vars.put("badge", isGroup ? "组内采购" : "冰箱采购");
        vars.put("headline", isGroup ? "分组采购清单已确认" : "冰箱采购清单已确认");
        String sender = senderNickname == null || senderNickname.isBlank() ? "您的家人" : senderNickname;
        String groupPart = (isGroup && groupName != null && !groupName.isBlank())
            ? "（分组 <b>「" + groupName + "」</b>）"
            : "";
        vars.put("greeting", buildGreeting(recipientNickname));
        vars.put("body", "<b>" + safe(sender) + "</b> " + groupPart
            + " 已确认本次采购清单，共 <b>" + (items == null ? 0 : items.size()) + "</b> 项。"
            + "请留意冰箱库存，适时补充。");
        vars.put("hint", isGroup
            ? "您作为分组成员已被通知，可访问「家庭中枢 / 采购历史」查看明细。"
            : "您可以访问「家庭中枢 / 采购历史」查看明细与历史复用。");
        vars.put("itemsTable", renderItemsTable(items));
        vars.put("year", EmailRenderSupport.currentYear());
        return vars;
    }

    private String renderItemsTable(List<PurchaseItem> items) {
        if (items == null || items.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"")
          .append(" style=\"background:#ffffff;border:1px solid #fed7aa;border-radius:10px;overflow:hidden;margin-top:8px;\">")
          .append("<tr style=\"background:#fff7ed;\">")
          .append("<th align=\"left\" style=\"padding:10px 16px;color:#9ca3af;font-size:12px;font-weight:600;letter-spacing:1px;\">物品</th>")
          .append("<th align=\"center\" style=\"padding:10px 16px;color:#9ca3af;font-size:12px;font-weight:600;letter-spacing:1px;width:80px;\">数量</th>")
          .append("</tr>");
        for (int i = 0; i < items.size(); i++) {
            PurchaseItem it = items.get(i);
            String rowBg = (i % 2 == 0) ? "#ffffff" : "#fffaf2";
            sb.append("<tr style=\"background:").append(rowBg).append(";\">")
              .append("<td style=\"padding:10px 16px;color:#1f2937;font-size:14px;\">").append(escape(it.getName())).append("</td>")
              .append("<td align=\"center\" style=\"padding:10px 16px;color:#f97316;font-size:14px;font-weight:600;\">")
              .append(it.getQuantity())
              .append(it.getUnit() == null || it.getUnit().isBlank() ? "" : " " + escape(it.getUnit()))
              .append("</td>")
              .append("</tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String buildGreeting(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return "您好，";
        }
        return "Hi " + nickname + "，";
    }

    public static List<PurchaseItem> itemsOf() {
        return new ArrayList<>();
    }
}
