package com.homecentral.fridge.service;

import com.homecentral.fridge.api.dto.FridgeRecognizeRequest;
import com.homecentral.fridge.api.vo.FridgeRecognizeResult;
import com.homecentral.fridge.api.vo.FridgeRecognizeVO;

import java.util.List;

public interface IFridgeRecognizeService {

    /**
     * 拍照识别入口：先调 VLM，再尝试用 suggestedCategoryName 匹配系统预置分类的 ID。
     * 始终返回识别结果；分类匹配为 best-effort。
     */
    FridgeRecognizeResult recognize(FridgeRecognizeRequest request);
}
