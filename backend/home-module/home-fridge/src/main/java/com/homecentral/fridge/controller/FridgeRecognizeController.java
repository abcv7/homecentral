package com.homecentral.fridge.controller;

import com.homecentral.common.model.Result;
import com.homecentral.fridge.api.dto.FridgeRecognizeRequest;
import com.homecentral.fridge.api.vo.FridgeRecognizeResult;
import com.homecentral.fridge.service.IFridgeRecognizeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "食材识别", description = "拍照 AI 识别食材")
@RestController
@RequestMapping("/api/fridge/recognize")
public class FridgeRecognizeController {

    private final IFridgeRecognizeService recognizeService;

    public FridgeRecognizeController(IFridgeRecognizeService recognizeService) {
        this.recognizeService = recognizeService;
    }

    @Operation(summary = "识别食材", description = "上传图片 base64，AI 返回预填的食材列表（前端可二次编辑）")
    @PostMapping
    public Result<FridgeRecognizeResult> recognize(@RequestBody FridgeRecognizeRequest request) {
        if (request == null || request.getImageBase64() == null || request.getImageBase64().isBlank()) {
            return Result.bizError("PARAM_MISSING", "请提供 imageBase64");
        }
        return Result.ok(recognizeService.recognize(request));
    }
}
