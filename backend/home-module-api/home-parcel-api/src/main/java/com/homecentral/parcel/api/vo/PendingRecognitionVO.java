package com.homecentral.parcel.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(description = "待导入识别任务")
public class PendingRecognitionVO {

    @Schema(description = "任务 ID")
    private Long id;

    @Schema(description = "状态: processing | completed | failed | imported | cancelled")
    private String status;

    @Schema(description = "识别结果 JSON（status=completed 时有值，前端反序列化为 ParcelRecognitionResult）")
    private String resultJson;

    @Schema(description = "失败原因（status=failed 时有值）")
    private String failureMessage;

    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;

    @Schema(description = "识别完成时间")
    private OffsetDateTime completedAt;

    @Schema(description = "过期时间（24h 后由定时任务自动落库）")
    private OffsetDateTime expiresAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResultJson() { return resultJson; }
    public void setResultJson(String resultJson) { this.resultJson = resultJson; }
    public String getFailureMessage() { return failureMessage; }
    public void setFailureMessage(String failureMessage) { this.failureMessage = failureMessage; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; }
}
