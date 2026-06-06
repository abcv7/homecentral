package com.homecentral.fridge.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "模板变更批量迁移结果")
public class FridgeMigrateResultVO {

    @Schema(description = "被扫描的食材总数（仅 ACTIVE 状态）")
    private int scannedCount;

    @Schema(description = "实际被迁出到采购篮的食材数")
    private int migratedCount;

    public FridgeMigrateResultVO() {}

    public FridgeMigrateResultVO(int scannedCount, int migratedCount) {
        this.scannedCount = scannedCount;
        this.migratedCount = migratedCount;
    }

    public int getScannedCount() { return scannedCount; }
    public void setScannedCount(int scannedCount) { this.scannedCount = scannedCount; }
    public int getMigratedCount() { return migratedCount; }
    public void setMigratedCount(int migratedCount) { this.migratedCount = migratedCount; }
}
