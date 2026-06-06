package com.homecentral.workshop.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "提交评价")
public class SubmitRatingRequest {

    @Schema(description = "口感 1-5 星", example = "5", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "1", maximum = "5")
    @NotNull
    @Min(1) @Max(5)
    private Short tasteScore;

    @Schema(description = "形象 1-5 星", example = "4", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "1", maximum = "5")
    @NotNull
    @Min(1) @Max(5)
    private Short appearanceScore;

    @Schema(description = "评语", example = "酸度平衡,杯口那圈糖霜很赞")
    @Size(max = 1000)
    private String comment;

    @Schema(description = "照片 file_id 列表 (走 home-file 上传,最多 9 张)")
    private List<String> photoFileIds;

    public Short getTasteScore() { return tasteScore; }
    public void setTasteScore(Short tasteScore) { this.tasteScore = tasteScore; }
    public Short getAppearanceScore() { return appearanceScore; }
    public void setAppearanceScore(Short appearanceScore) { this.appearanceScore = appearanceScore; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public List<String> getPhotoFileIds() { return photoFileIds; }
    public void setPhotoFileIds(List<String> photoFileIds) { this.photoFileIds = photoFileIds; }
}
