package com.homecentral.parcel.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "快递识别请求（图片或短信文本）")
public class ParcelRecognitionRequest {

    @Schema(description = "图片的base64编码（与smsText二选一）")
    private String imageBase64;

    @Schema(description = "短信文本内容（与imageBase64二选一）")
    private String smsText;

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getSmsText() {
        return smsText;
    }

    public void setSmsText(String smsText) {
        this.smsText = smsText;
    }
}
