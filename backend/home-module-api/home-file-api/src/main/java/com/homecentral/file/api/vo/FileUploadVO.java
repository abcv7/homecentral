package com.homecentral.file.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "文件上传响应")
public class FileUploadVO {

    @Schema(description = "文件ID")
    private String fileId;

    @Schema(description = "对象存储Key")
    private String objectKey;

    @Schema(description = "访问URL")
    private String accessUrl;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public String getAccessUrl() {
        return accessUrl;
    }

    public void setAccessUrl(String accessUrl) {
        this.accessUrl = accessUrl;
    }
}
