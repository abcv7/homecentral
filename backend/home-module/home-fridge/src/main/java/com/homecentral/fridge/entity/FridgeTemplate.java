package com.homecentral.fridge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.homecentral.fridge.api.enums.FridgeLayout;

import java.time.OffsetDateTime;

@TableName(value = "fridge_template", schema = "home_fridge")
public class FridgeTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long ownerId;
    private String name;
    private FridgeLayout layout;
    private Integer fridgeLayers;
    private Integer freezerLayers;
    private Integer chillerLayers;
    private Integer doorShelfCount;
    private Boolean isDefault;
    private Boolean isSystem;
    private OffsetDateTime createdAt;
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
    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
    public Boolean getIsSystem() { return isSystem; }
    public void setIsSystem(Boolean isSystem) { this.isSystem = isSystem; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
