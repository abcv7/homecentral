package com.homecentral.friend.api.dto;

import com.homecentral.friend.api.enums.GroupType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "创建/更新分组请求")
public class FriendGroupRequest {

    @NotBlank
    @Size(max = 50)
    @Schema(description = "分组名称", example = "家人")
    private String name;

    @Schema(description = "分组类型（FRIEND/COUPLE/FAMILY/CUSTOM）", example = "FAMILY")
    private GroupType type = GroupType.FRIEND;

    @Size(max = 7)
    @Schema(description = "分组颜色（HEX）", example = "#FF6B6B")
    private String color;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public GroupType getType() { return type; }
    public void setType(GroupType type) { this.type = type; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
