package com.homecentral.fridge.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "创建自定义分类请求")
public class FridgeCategoryRequest {

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "烘焙原料")
    @NotBlank
    @Size(max = 100)
    private String name;

    @Schema(description = "图标 emoji 或 url", example = "🍞")
    @Size(max = 50)
    private String icon;

    @Schema(description = "颜色 hex", example = "#F59E0B")
    @Size(max = 20)
    private String color;

    @Schema(description = "排序权重，值越小越靠前", example = "0")
    private Integer sortOrder;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
