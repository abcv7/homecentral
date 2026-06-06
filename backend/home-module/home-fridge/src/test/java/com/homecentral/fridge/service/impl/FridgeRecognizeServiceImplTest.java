package com.homecentral.fridge.service.impl;

import com.homecentral.fridge.api.dto.FridgeRecognizeRequest;
import com.homecentral.fridge.api.enums.FridgeZone;
import com.homecentral.fridge.api.vo.FridgeCategoryVO;
import com.homecentral.fridge.api.vo.FridgeRecognizeResult;
import com.homecentral.fridge.api.vo.FridgeRecognizeVO;
import com.homecentral.fridge.recognition.FoodVlmClient;
import com.homecentral.fridge.service.IFridgeCategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FridgeRecognizeServiceImplTest {

    @Mock
    private FoodVlmClient foodVlmClient;

    @Mock
    private IFridgeCategoryService categoryService;

    @InjectMocks
    private FridgeRecognizeServiceImpl service;

    private FridgeRecognizeVO newVo(String name, String categoryName) {
        FridgeRecognizeVO vo = new FridgeRecognizeVO();
        vo.setName(name);
        vo.setSuggestedCategoryName(categoryName);
        vo.setEstimatedQuantity(BigDecimal.ONE);
        vo.setEstimatedUnit("个");
        return vo;
    }

    private FridgeCategoryVO newCategory(Long id, String name) {
        FridgeCategoryVO c = new FridgeCategoryVO();
        c.setId(id);
        c.setName(name);
        return c;
    }

    private FridgeRecognizeRequest reqWithImage() {
        FridgeRecognizeRequest r = new FridgeRecognizeRequest();
        r.setImageBase64("data:image/jpeg;base64,dummy");
        return r;
    }

    @Test
    void shouldEnrichCategoryByName() {
        FridgeCategoryVO veg = newCategory(1L, "蔬菜");
        when(foodVlmClient.recognize(any())).thenReturn(List.of(
                newVo("西红柿", "蔬菜"),
                newVo("纯牛奶", "乳制品")));
        when(categoryService.findAll()).thenReturn(List.of(veg, newCategory(2L, "乳制品")));

        FridgeRecognizeResult result = service.recognize(reqWithImage());

        assertEquals(2, result.getItems().size());
        assertEquals(1L, result.getItems().get(0).getSuggestedCategoryId());
        assertEquals(2L, result.getItems().get(1).getSuggestedCategoryId());
    }

    @Test
    void shouldKeepOriginalCategoryNameWhenNoMatch() {
        FridgeRecognizeVO vo = newVo("自制酸菜", "腌制食品");
        when(foodVlmClient.recognize(any())).thenReturn(List.of(vo));
        when(categoryService.findAll()).thenReturn(List.of(newCategory(1L, "蔬菜")));

        FridgeRecognizeResult result = service.recognize(reqWithImage());

        assertEquals(1, result.getItems().size());
        assertNull(result.getItems().get(0).getSuggestedCategoryId());
        assertEquals("腌制食品", result.getItems().get(0).getSuggestedCategoryName());
    }

    @Test
    void shouldApplyDefaultsWhenFieldsAreMissing() {
        FridgeRecognizeVO vo = new FridgeRecognizeVO();
        vo.setName("酸奶");
        when(foodVlmClient.recognize(any())).thenReturn(List.of(vo));
        when(categoryService.findAll()).thenReturn(List.of());

        FridgeRecognizeResult result = service.recognize(reqWithImage());

        FridgeRecognizeVO got = result.getItems().get(0);
        assertEquals(FridgeZone.REFRIGERATED, got.getSuggestedZone());
        assertEquals(BigDecimal.ONE, got.getEstimatedQuantity());
        assertEquals("个", got.getEstimatedUnit());
        assertNotNull(got.getPurchaseDate());
    }

    @Test
    void shouldReturnEmptyListOnBlankRequest() {
        FridgeRecognizeResult result = service.recognize(new FridgeRecognizeRequest());
        assertEquals(0, result.getTotalCount());
    }

    @Test
    void shouldReturnEmptyListOnVlmFailure() {
        FridgeRecognizeResult result = service.recognize(reqWithImage());
        assertEquals(0, result.getTotalCount());
    }
}
