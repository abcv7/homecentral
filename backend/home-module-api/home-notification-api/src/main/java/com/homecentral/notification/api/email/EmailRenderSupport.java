package com.homecentral.notification.api.email;

import java.time.LocalDate;

public final class EmailRenderSupport {

    private EmailRenderSupport() {
    }

    public static String renderCodeBoxes(String code) {
        if (code == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int len = code.length();
        for (int i = 0; i < len; i++) {
            char c = code.charAt(i);
            sb.append("<td align=\"center\" width=\"56\" style=\"padding:0 4px;\">")
              .append("<div style=\"width:48px;height:56px;line-height:56px;background:#ffffff;border:2px solid #fed7aa;border-radius:10px;color:#ef4444;font-size:28px;font-weight:700;text-align:center;font-family:'SF Mono','Menlo','Consolas',monospace;\">")
              .append(c)
              .append("</div></td>");
        }
        return sb.toString();
    }

    public static int currentYear() {
        return LocalDate.now().getYear();
    }
}
