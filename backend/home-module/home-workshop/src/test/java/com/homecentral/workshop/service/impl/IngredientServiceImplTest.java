package com.homecentral.workshop.service.impl;

import com.homecentral.workshop.api.vo.IngredientVO;
import com.homecentral.workshop.entity.WorkshopIngredient;
import com.homecentral.workshop.mapper.WorkshopIngredientMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngredientServiceImplTest {

    @Mock
    private WorkshopIngredientMapper ingredientMapper;

    @InjectMocks
    private IngredientServiceImpl service;

    @Test
    void listAll_returnsAllIngredients() {
        WorkshopIngredient gin = ingredient(10L, "Gin", "金酒");
        WorkshopIngredient tonic = ingredient(20L, "Tonic", "汤力水");
        when(ingredientMapper.selectList(null)).thenReturn(List.of(gin, tonic));

        List<IngredientVO> result = service.listAll();

        assertEquals(2, result.size());
        assertEquals(10L, result.get(0).getId());
        assertEquals("Gin", result.get(0).getNameEn());
        assertEquals("金酒", result.get(0).getNameZh());
        assertEquals(20L, result.get(1).getId());
    }

    @Test
    void listAll_emptyDbReturnsEmpty() {
        when(ingredientMapper.selectList(null)).thenReturn(Collections.emptyList());
        assertTrue(service.listAll().isEmpty());
    }

    @Test
    void listAll_nullAliasesBecomesEmptyList() {
        WorkshopIngredient ing = new WorkshopIngredient();
        ing.setId(1L);
        ing.setNameZh("Test");
        ing.setNameEn("TestEn");
        ing.setAliases(null);
        ing.setDefaultBottleMl(new BigDecimal("750"));
        ing.setCocktailCount(0);
        when(ingredientMapper.selectList(null)).thenReturn(List.of(ing));

        List<IngredientVO> result = service.listAll();

        assertEquals(1, result.size());
        assertNotNull(result.get(0).getAliases());
        assertTrue(result.get(0).getAliases().isEmpty());
    }

    private WorkshopIngredient ingredient(long id, String nameEn, String nameZh) {
        WorkshopIngredient i = new WorkshopIngredient();
        i.setId(id);
        i.setNameEn(nameEn);
        i.setNameZh(nameZh);
        return i;
    }
}
