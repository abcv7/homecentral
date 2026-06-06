package com.homecentral.fridge.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "临期/过期统计")
public class FridgeExpiringVO {

    @Schema(description = "已过期数量")
    private long expiredCount;

    @Schema(description = "临期数量（含今天到 N 天）")
    private long expiringCount;

    @Schema(description = "查询时使用的天数阈值")
    private int thresholdDays;

    public FridgeExpiringVO() {}

    public FridgeExpiringVO(long expiredCount, long expiringCount, int thresholdDays) {
        this.expiredCount = expiredCount;
        this.expiringCount = expiringCount;
        this.thresholdDays = thresholdDays;
    }

    public long getExpiredCount() { return expiredCount; }
    public void setExpiredCount(long expiredCount) { this.expiredCount = expiredCount; }
    public long getExpiringCount() { return expiringCount; }
    public void setExpiringCount(long expiringCount) { this.expiringCount = expiringCount; }
    public int getThresholdDays() { return thresholdDays; }
    public void setThresholdDays(int thresholdDays) { this.thresholdDays = thresholdDays; }
}
