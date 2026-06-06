package com.homecentral.workshop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homecentral.workshop.api.dto.RecommendRequest;
import com.homecentral.workshop.api.vo.CocktailVO;
import com.homecentral.workshop.api.vo.IngredientVO;
import com.homecentral.workshop.api.vo.RecommendResultVO;
import com.homecentral.workshop.service.IRecommendService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = RecommendController.class, properties = {
    "spring.cloud.nacos.config.enabled=false",
    "spring.cloud.nacos.discovery.enabled=false",
    "spring.cloud.config.import-check.enabled=false"
})
class RecommendControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private IRecommendService recommendService;

    @Test
    void shouldReturnRecommendResults() throws Exception {
        RecommendResultVO vo = new RecommendResultVO();
        vo.setTier("full");
        CocktailVO cocktail = new CocktailVO();
        cocktail.setId(1L);
        cocktail.setNameZh("Gin Tonic");
        cocktail.setNameEn("Gin and Tonic");
        cocktail.setViews(100);
        vo.setCocktail(cocktail);
        IngredientVO ing = new IngredientVO();
        ing.setId(10L);
        ing.setNameZh("Gin");
        vo.setMatchedIngredients(List.of(ing));
        vo.setMissingIngredients(List.of());
        vo.setMissingCount(BigDecimal.ZERO);

        when(recommendService.recommend(any(RecommendRequest.class))).thenReturn(List.of(vo));

        RecommendRequest req = new RecommendRequest();
        req.setIngredientIds(List.of(10L, 20L));
        req.setMode("STRICT");

        mockMvc.perform(post("/api/workshop/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].tier").value("full"))
                .andExpect(jsonPath("$.data[0].cocktail.id").value(1))
                .andExpect(jsonPath("$.data[0].matchedIngredients[0].nameZh").value("Gin"))
                .andExpect(jsonPath("$.data[0].missingCount").value(0));
    }

    @Test
    void shouldReturnEmptyListWhenServiceReturnsEmpty() throws Exception {
        when(recommendService.recommend(any(RecommendRequest.class))).thenReturn(List.of());

        RecommendRequest req = new RecommendRequest();
        req.setIngredientIds(List.of(999L));
        req.setMode("MAIN");

        mockMvc.perform(post("/api/workshop/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void shouldCallServiceOnce() throws Exception {
        when(recommendService.recommend(any(RecommendRequest.class))).thenReturn(List.of());

        RecommendRequest req = new RecommendRequest();
        req.setIngredientIds(List.of(1L));
        req.setMode("MAIN");

        mockMvc.perform(post("/api/workshop/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(recommendService, times(1)).recommend(any(RecommendRequest.class));
    }
}
