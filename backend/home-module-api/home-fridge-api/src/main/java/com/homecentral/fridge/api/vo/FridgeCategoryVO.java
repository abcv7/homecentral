package com.homecentral.fridge.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "食材分类视图")
public class FridgeCategoryVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "图标 emoji 或 url")
    private String icon;

    @Schema(description = "颜色 hex")
    private String color;

    @Schema(description = "排序权重")
    private Integer sortOrder;

    @Schema(description = "是否系统预置（系统分类不可改/删）")
    private boolean system;

    @Schema(description = "创建人（系统分类为 null）")
    private Long createdBy;

    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public boolean isSystem() { return system; }
    public void setSystem(boolean system) { this.system = system; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
