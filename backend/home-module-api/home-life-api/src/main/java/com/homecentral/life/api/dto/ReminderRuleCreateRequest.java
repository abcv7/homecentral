package com.homecentral.life.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "创建提醒规则请求")
public class ReminderRuleCreateRequest {

    @Schema(description = "提醒标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "喝水提醒")
    @NotBlank
    private String title;

    @Schema(description = "提醒内容", example = "记得喝水哦")
    private String content;

    @Schema(description = "Cron表达式", requiredMode = Schema.RequiredMode.REQUIRED, example = "0 0 9 * * ?")
    @NotBlank
    private String cronExpression;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled = true;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCronExpression() { return cronExpression; }
    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
