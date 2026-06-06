package com.homecentral.notification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.OffsetDateTime;

@TableName(value = "notification", schema = "home_notification")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private String content;
    private String sourceType;
    private String sourceId;
    private Boolean read;
    private OffsetDateTime notifyTime;
    private Long userId;
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }
    public Boolean getRead() { return read; }
    public void setRead(Boolean read) { this.read = read; }
    public OffsetDateTime getNotifyTime() { return notifyTime; }
    public void setNotifyTime(OffsetDateTime notifyTime) { this.notifyTime = notifyTime; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
