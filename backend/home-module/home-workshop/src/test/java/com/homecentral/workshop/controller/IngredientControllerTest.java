package com.homecentral.workshop.controller;

import com.homecentral.workshop.api.vo.IngredientVO;
import com.homecentral.workshop.service.IIngredientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = IngredientController.class, properties = {
    "spring.cloud.nacos.config.enabled=false",
    "spring.cloud.nacos.discovery.enabled=false",
    "spring.cloud.config.import-check.enabled=false"
})
class IngredientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IIngredientService ingredientService;

    @Test
    void shouldListAllIngredients() throws Exception {
        IngredientVO gin = new IngredientVO();
        gin.setId(10L);
        gin.setNameZh("金酒");
        gin.setNameEn("Gin");
        IngredientVO tonic = new IngredientVO();
        tonic.setId(20L);
        tonic.setNameZh("汤力水");
        tonic.setNameEn("Tonic");
        when(ingredientService.listAll()).thenReturn(List.of(gin, tonic));

        mockMvc.perform(get("/api/workshop/ingredients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(10))
                .andExpect(jsonPath("$.data[0].nameZh").value("金酒"))
                .andExpect(jsonPath("$.data[1].id").value(20));
    }

    @Test
    void shouldReturnEmptyWhenNoIngredients() throws Exception {
        when(ingredientService.listAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/workshop/ingredients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}
