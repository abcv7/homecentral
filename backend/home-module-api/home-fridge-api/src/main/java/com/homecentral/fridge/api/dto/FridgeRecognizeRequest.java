package com.homecentral.fridge.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "拍照识别请求（图片 base64）")
public class FridgeRecognizeRequest {

    @Schema(description = "图片 base64 编码（无 data:image/xxx;base64, 前缀）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String imageBase64;

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
}
