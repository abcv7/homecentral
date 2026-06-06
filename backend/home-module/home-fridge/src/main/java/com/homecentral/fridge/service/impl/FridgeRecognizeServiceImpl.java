package com.homecentral.fridge.service.impl;

import com.homecentral.fridge.api.dto.FridgeRecognizeRequest;
import com.homecentral.fridge.api.enums.FridgeZone;
import com.homecentral.fridge.api.vo.FridgeCategoryVO;
import com.homecentral.fridge.api.vo.FridgeRecognizeResult;
import com.homecentral.fridge.api.vo.FridgeRecognizeVO;
import com.homecentral.fridge.recognition.FoodVlmClient;
import com.homecentral.fridge.service.IFridgeCategoryService;
import com.homecentral.fridge.service.IFridgeRecognizeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FridgeRecognizeServiceImpl implements IFridgeRecognizeService {

    private static final Logger log = LoggerFactory.getLogger(FridgeRecognizeServiceImpl.class);

    private final FoodVlmClient foodVlmClient;
    private final IFridgeCategoryService categoryService;

    public FridgeRecognizeServiceImpl(FoodVlmClient foodVlmClient, IFridgeCategoryService categoryService) {
        this.foodVlmClient = foodVlmClient;
        this.categoryService = categoryService;
    }

    @Override
    public FridgeRecognizeResult recognize(FridgeRecognizeRequest request) {
        if (request == null || request.getImageBase64() == null || request.getImageBase64().isBlank()) {
            return new FridgeRecognizeResult(List.of());
        }

        List<FridgeRecognizeVO> raw = foodVlmClient.recognize(request.getImageBase64());
        if (raw.isEmpty()) {
            return new FridgeRecognizeResult(List.of());
        }

        List<FridgeCategoryVO> categories = categoryService.findAll();
        Map<String, FridgeCategoryVO> categoryByName = categories.stream()
                .filter(c -> c.getName() != null)
                .collect(Collectors.toMap(c -> c.getName().trim(), c -> c, (a, b) -> a));

        List<FridgeRecognizeVO> enriched = new ArrayList<>();
        for (FridgeRecognizeVO vo : raw) {
            enrich(vo, categoryByName);
            enriched.add(vo);
        }
        log.info("Recognition finished, returned {} items", enriched.size());
        return new FridgeRecognizeResult(enriched);
    }

    private void enrich(FridgeRecognizeVO vo, Map<String, FridgeCategoryVO> categoryByName) {
        if (vo == null) {
            return;
        }
        if (vo.getSuggestedZone() == null) {
            vo.setSuggestedZone(FridgeZone.REFRIGERATED);
        }
        if (vo.getPurchaseDate() == null) {
            vo.setPurchaseDate(LocalDate.now());
        }
        if (vo.getEstimatedQuantity() == null) {
            vo.setEstimatedQuantity(BigDecimal.ONE);
        }
        if (vo.getEstimatedUnit() == null || vo.getEstimatedUnit().isBlank()) {
            vo.setEstimatedUnit("个");
        }
        if (vo.getSuggestedCategoryName() != null) {
            FridgeCategoryVO matched = categoryByName.get(vo.getSuggestedCategoryName().trim());
            if (matched != null) {
                vo.setSuggestedCategoryId(matched.getId());
                vo.setSuggestedCategoryName(matched.getName());
            }
        }
    }
}
