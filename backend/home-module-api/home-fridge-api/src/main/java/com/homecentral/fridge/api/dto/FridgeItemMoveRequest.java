package com.homecentral.fridge.api.dto;

import com.homecentral.fridge.api.enums.FridgeZone;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "拖拽移动食材请求")
public class FridgeItemMoveRequest {

    @Schema(description = "目标区域；null 表示回采购篮", example = "FROZEN")
    private FridgeZone zone;

    @Schema(description = "目标子区域（层/门编码），例如 REFRIGERATED-L1 / DOOR-LEFT-L1；null 表示回采购篮",
            example = "FROZEN-L1")
    @Size(max = 50)
    private String subZone;

    public FridgeZone getZone() { return zone; }
    public void setZone(FridgeZone zone) { this.zone = zone; }
    public String getSubZone() { return subZone; }
    public void setSubZone(String subZone) { this.subZone = subZone; }
}
