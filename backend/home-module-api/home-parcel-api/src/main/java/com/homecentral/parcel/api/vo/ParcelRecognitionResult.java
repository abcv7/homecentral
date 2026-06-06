package com.homecentral.parcel.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "快递识别总结果")
public class ParcelRecognitionResult {

    @Schema(description = "服务端待导入任务 ID（用于前端在自动导入/认领时定位服务端记录；userId 为空时该字段为 null）")
    private Long pendingId;

    @Schema(description = "识别出的快递列表")
    private List<ParcelRecognitionVO> parcels;

    @Schema(description = "识别总数")
    private int totalCount;

    @Schema(description = "OCR原始文本（调试用）")
    private String rawOcrText;

    @Schema(description = "VLM原始返回（调试用）")
    private String rawVlmResult;

    public ParcelRecognitionResult() {}

    public ParcelRecognitionResult(List<ParcelRecognitionVO> parcels) {
        this.parcels = parcels;
        this.totalCount = parcels != null ? parcels.size() : 0;
    }

    public Long getPendingId() { return pendingId; }
    public void setPendingId(Long pendingId) { this.pendingId = pendingId; }
    public List<ParcelRecognitionVO> getParcels() { return parcels; }
    public void setParcels(List<ParcelRecognitionVO> parcels) { this.parcels = parcels; }
    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    public String getRawOcrText() { return rawOcrText; }
    public void setRawOcrText(String rawOcrText) { this.rawOcrText = rawOcrText; }
    public String getRawVlmResult() { return rawVlmResult; }
    public void setRawVlmResult(String rawVlmResult) { this.rawVlmResult = rawVlmResult; }
}
