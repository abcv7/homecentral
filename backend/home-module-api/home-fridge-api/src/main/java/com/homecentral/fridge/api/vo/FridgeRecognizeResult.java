package com.homecentral.fridge.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "拍照识别总结果")
public class FridgeRecognizeResult {

    @Schema(description = "识别出的食材列表")
    private List<FridgeRecognizeVO> items;

    @Schema(description = "识别总数")
    private int totalCount;

    public FridgeRecognizeResult() {}

    public FridgeRecognizeResult(List<FridgeRecognizeVO> items) {
        this.items = items;
        this.totalCount = items != null ? items.size() : 0;
    }

    public List<FridgeRecognizeVO> getItems() { return items; }
    public void setItems(List<FridgeRecognizeVO> items) { this.items = items; }
    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
}
