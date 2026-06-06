package com.homecentral.fridge.api.dto;

import com.homecentral.fridge.api.enums.FridgeLayout;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "冰箱模板请求（创建/更新）")
public class FridgeTemplateRequest {

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "我家冰箱")
    @NotBlank
    @Size(max = 64)
    private String name;

    @Schema(description = "门型", requiredMode = Schema.RequiredMode.REQUIRED, example = "CLASSIC")
    @NotNull
    private FridgeLayout layout;

    @Schema(description = "冷藏层层数 1-5", example = "3")
    @Min(1) @Max(5)
    private Integer fridgeLayers = 3;

    @Schema(description = "冷冻层层数 1-5", example = "2")
    @Min(1) @Max(5)
    private Integer freezerLayers = 2;

    @Schema(description = "解冻层层数 0-3（仅 THREE_DOOR 模板有效）", example = "0")
    @Min(0) @Max(3)
    private Integer chillerLayers = 0;

    @Schema(description = "每侧门搁板数 0-5", example = "3")
    @Min(0) @Max(5)
    private Integer doorShelfCount = 3;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
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
