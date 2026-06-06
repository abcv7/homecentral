package com.homecentral.notification.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(description = "通知视图")
public class NotificationVO {

    @Schema(description = "通知ID")
    private Long id;

    @Schema(description = "通知标题")
    private String title;

    @Schema(description = "通知内容")
    private String content;

    @Schema(description = "是否已读")
    private boolean read;

    @Schema(description = "通知时间")
    private OffsetDateTime notifyTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public OffsetDateTime getNotifyTime() {
        return notifyTime;
    }

    public void setNotifyTime(OffsetDateTime notifyTime) {
        this.notifyTime = notifyTime;
    }
}
