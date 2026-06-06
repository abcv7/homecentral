package com.homecentral.fridge.api.dto;

import com.homecentral.fridge.api.enums.FridgeLayout;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "模板变更触发的批量迁移请求")
public class FridgeMigrateRequest {

    @Schema(description = "目标模板 ID（仅做日志追踪，实际以 layout/层数为准）", example = "5")
    private Long templateId;

    @NotNull
    @Schema(description = "新门型")
    private FridgeLayout layout;

    @Min(1) @Max(5)
    @Schema(description = "新冷藏层层数")
    private Integer fridgeLayers;

    @Min(1) @Max(5)
    @Schema(description = "新冷冻层层数")
    private Integer freezerLayers;

    @Min(0) @Max(3)
    @Schema(description = "新解冻层层数")
    private Integer chillerLayers;

    @Min(0) @Max(5)
    @Schema(description = "新每侧门搁板数")
    private Integer doorShelfCount;

    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }
    public FridgeLayout getLayout() { return layout; }
    public void setLayout(FridgeLayout layout) { this.layout = layout; }
    public Integer getFridgeLayers() { return fridgeLayers; }
    public void setFridgeLayers(Integer fridgeLayers) { this.fridgeLayers = fridgeLayers; }
    public Integer getFreezerLayers() { return freezerLayers; }
    public void setFreezerLayers(Integer freezerLayers) { this.freezerLayers = freezerLayers; }
    public Integer getChillerLayers() { return chillerLayers; }
    public void setChillerLayers(Integer chillerLayers) { this.chillerLayers = chillerLayers; }
    public Integer getDoorShelfCount() { return doorShelfCount; }
    public void setDoorShelfCount(Integer doorShelfCount) { this.doorShelfCount = doorShelfCount; }
}
