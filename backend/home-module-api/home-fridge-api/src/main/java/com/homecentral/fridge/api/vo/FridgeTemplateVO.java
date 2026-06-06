package com.homecentral.fridge.api.vo;

import com.homecentral.fridge.api.enums.FridgeLayout;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "冰箱模板视图")
public class FridgeTemplateVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "创建人（系统预置为 null）")
    private Long ownerId;

    @Schema(description = "模板名称")
    private String name;

    @Schema(description = "门型")
    private FridgeLayout layout;

    @Schema(description = "冷藏层层数")
    private Integer fridgeLayers;

    @Schema(description = "冷冻层层数")
    private Integer freezerLayers;

    @Schema(description = "解冻层层数")
    private Integer chillerLayers;

    @Schema(description = "每侧门搁板数")
    private Integer doorShelfCount;

    @Schema(description = "是否当前用户激活的默认模板")
    private boolean isDefault;

    @Schema(description = "是否系统预置")
    private boolean isSystem;

    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;

    @Schema(description = "更新时间")
    private OffsetDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
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
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }
    public boolean isSystem() { return isSystem; }
    public void setSystem(boolean system) { isSystem = system; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
