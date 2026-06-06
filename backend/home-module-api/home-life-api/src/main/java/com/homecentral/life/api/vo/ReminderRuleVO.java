package com.homecentral.life.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(description = "提醒规则视图")
public class ReminderRuleVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "提醒标题")
    private String title;

    @Schema(description = "提醒内容")
    private String content;

    @Schema(description = "Cron表达式")
    private String cronExpression;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "最后触发时间")
    private OffsetDateTime lastTriggeredAt;

    @Schema(description = "创建人ID")
    private Long createdBy;

    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCronExpression() { return cronExpression; }
    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public OffsetDateTime getLastTriggeredAt() { return lastTriggeredAt; }
    public void setLastTriggeredAt(OffsetDateTime lastTriggeredAt) { this.lastTriggeredAt = lastTriggeredAt; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
