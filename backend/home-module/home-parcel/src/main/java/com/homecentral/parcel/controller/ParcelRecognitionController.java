package com.homecentral.parcel.controller;

import com.homecentral.common.model.BatchResult;
import com.homecentral.common.model.Result;
import com.homecentral.parcel.api.dto.ParcelCreateRequest;
import com.homecentral.parcel.api.dto.ParcelRecognitionRequest;
import com.homecentral.parcel.api.vo.ParcelRecognitionResult;
import com.homecentral.parcel.api.vo.ParcelSummaryVO;
import com.homecentral.parcel.api.vo.PendingRecognitionVO;
import com.homecentral.parcel.recognition.ParcelRecognitionService;
import com.homecentral.parcel.service.IParcelService;
import com.homecentral.parcel.service.IPendingRecognitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "快递识别", description = "AI图片识别快递信息")
@RestController
@RequestMapping("/api/parcel")
public class ParcelRecognitionController {

    private final ParcelRecognitionService recognitionService;
    private final IParcelService parcelService;
    private final IPendingRecognitionService pendingService;

    public ParcelRecognitionController(ParcelRecognitionService recognitionService,
                                        IParcelService parcelService,
                                        IPendingRecognitionService pendingService) {
        this.recognitionService = recognitionService;
        this.parcelService = parcelService;
        this.pendingService = pendingService;
    }

    @Operation(summary = "识别快递", description = "上传图片base64或短信文本识别快递信息；结果会写入待导入表，关页面后下次进入可恢复")
    @PostMapping("/recognize")
    public Result<ParcelRecognitionResult> recognize(
            @RequestBody ParcelRecognitionRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (request.getSmsText() != null && !request.getSmsText().isBlank()) {
            return Result.ok(recognitionService.recognizeText(userId, request.getSmsText()));
        }
        if (request.getImageBase64() != null && !request.getImageBase64().isBlank()) {
            return Result.ok(recognitionService.recognize(userId, request.getImageBase64()));
        }
        return Result.bizError("PARAM_MISSING", "请提供 imageBase64 或 smsText");
    }

    @Operation(summary = "批量导入快递", description = "批量导入识别的快递信息，逐个校验运单号/取件码唯一性")
    @PostMapping("/batch-import")
    public Result<BatchResult<ParcelSummaryVO>> batchImport(
            @RequestBody List<ParcelCreateRequest> requests,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(parcelService.batchImport(requests, userId));
    }

    @Operation(summary = "查询待导入任务", description = "拉回当前用户所有未完成导入的识别任务（processing/completed/failed），用于关闭页面后恢复")
    @GetMapping("/recognition/pending")
    public Result<List<PendingRecognitionVO>> listPending(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) return Result.ok(List.of());
        return Result.ok(pendingService.listPending(userId));
    }

    @Operation(summary = "查询单个待导入任务", description = "用于轮询 processing 任务状态变化")
    @GetMapping("/recognition/{id}")
    public Result<PendingRecognitionVO> getRecognition(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) return Result.ok(null);
        PendingRecognitionVO vo = pendingService.getById(id, userId);
        if (vo == null) return Result.bizError("NOT_FOUND", "任务不存在");
        return Result.ok(vo);
    }

    @Operation(summary = "确认导入待导入任务", description = "用户在页面点击「确认导入」成功后调用，标记 pending 任务为 imported")
    @PostMapping("/recognition/{id}/claim")
    public Result<Void> claimRecognition(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) return Result.ok(null);
        PendingRecognitionVO vo = pendingService.getById(id, userId);
        if (vo == null) return Result.bizError("NOT_FOUND", "任务不存在");
        if (!"completed".equals(vo.getStatus())) {
            return Result.bizError("INVALID_STATE", "只能确认 completed 状态的任务，当前：" + vo.getStatus());
        }
        pendingService.markImported(id, null);
        return Result.ok(null);
    }

    @Operation(summary = "取消待导入任务", description = "用户主动放弃某个识别结果时调用，标记为 cancelled（24h 后清理）")
    @PostMapping("/recognition/{id}/discard")
    public Result<Void> discardRecognition(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) return Result.ok(null);
        PendingRecognitionVO vo = pendingService.getById(id, userId);
        if (vo == null) return Result.bizError("NOT_FOUND", "任务不存在");
        pendingService.markCancelled(id);
        return Result.ok(null);
    }
}
