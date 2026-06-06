package com.homecentral.workshop.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "创建点单请求")
public class CreateDrinkOrderRequest {

    @Schema(description = "制作者 userId", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long makerId;

    @Schema(description = "鸡尾酒 id", example = "128", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long cocktailId;

    @Schema(description = "好友分组 id (可空, 留空取默认分组)", example = "5")
    private Long groupId;

    @Schema(description = "留言 (如 '想微醺一下')", example = "想微醺一下")
    @Size(max = 500)
    private String message;

    public Long getMakerId() { return makerId; }
    public void setMakerId(Long makerId) { this.makerId = makerId; }
    public Long getCocktailId() { return cocktailId; }
    public void setCocktailId(Long cocktailId) { this.cocktailId = cocktailId; }
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
