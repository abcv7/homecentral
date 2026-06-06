package com.homecentral.life.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homecentral.life.api.dto.AnniversaryCreateRequest;
import com.homecentral.life.api.dto.ReminderRuleCreateRequest;
import com.homecentral.life.api.dto.ShoppingMemoCreateRequest;
import com.homecentral.life.api.vo.AnniversaryVO;
import com.homecentral.life.api.vo.ReminderRuleVO;
import com.homecentral.life.api.vo.ShoppingMemoVO;
import com.homecentral.life.service.ILifeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = LifeController.class, properties = {
    "spring.cloud.nacos.config.enabled=false",
    "spring.cloud.nacos.discovery.enabled=false",
    "spring.cloud.config.import-check.enabled=false"
})
class LifeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ILifeService lifeService;

    @Test
    void shouldCreateShoppingMemo() throws Exception {
        ShoppingMemoCreateRequest request = new ShoppingMemoCreateRequest();
        request.setItemName("苹果");

        ShoppingMemoVO vo = new ShoppingMemoVO();
        vo.setId(1L);
        vo.setItemName("苹果");
        vo.setPurchased(false);

        when(lifeService.createShoppingMemo(any(), eq(1L))).thenReturn(vo);

        mockMvc.perform(post("/api/life/shopping-memos")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.itemName").value("苹果"));
    }

    @Test
    void shouldReturn400WhenShoppingMemoItemNameBlank() throws Exception {
        ShoppingMemoCreateRequest request = new ShoppingMemoCreateRequest();
        request.setItemName("");

        mockMvc.perform(post("/api/life/shopping-memos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldListShoppingMemos() throws Exception {
        ShoppingMemoVO vo = new ShoppingMemoVO();
        vo.setId(1L);
        vo.setItemName("牛奶");

        when(lifeService.listShoppingMemos(1L)).thenReturn(List.of(vo));

        mockMvc.perform(get("/api/life/shopping-memos")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].itemName").value("牛奶"));
    }

    @Test
    void shouldTogglePurchased() throws Exception {
        ShoppingMemoVO vo = new ShoppingMemoVO();
        vo.setId(1L);
        vo.setItemName("牛奶");
        vo.setPurchased(true);

        when(lifeService.togglePurchased(1L, 1L)).thenReturn(vo);

        mockMvc.perform(put("/api/life/shopping-memos/1/toggle")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.purchased").value(true));
    }

    @Test
    void shouldCreateAnniversary() throws Exception {
        AnniversaryCreateRequest request = new AnniversaryCreateRequest();
        request.setTitle("纪念日");

        AnniversaryVO vo = new AnniversaryVO();
        vo.setId(1L);
        vo.setTitle("纪念日");

        when(lifeService.createAnniversary(any(), eq(1L))).thenReturn(vo);

        mockMvc.perform(post("/api/life/anniversaries")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("纪念日"));
    }

    @Test
    void shouldReturn400WhenAnniversaryTitleBlank() throws Exception {
        AnniversaryCreateRequest request = new AnniversaryCreateRequest();
        request.setTitle("");

        mockMvc.perform(post("/api/life/anniversaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldListAnniversaries() throws Exception {
        AnniversaryVO vo = new AnniversaryVO();
        vo.setId(1L);
        vo.setTitle("春节");

        when(lifeService.listAnniversaries(1L)).thenReturn(List.of(vo));

        mockMvc.perform(get("/api/life/anniversaries")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("春节"));
    }

    @Test
    void shouldCreateReminderRule() throws Exception {
        ReminderRuleCreateRequest request = new ReminderRuleCreateRequest();
        request.setTitle("提醒");
        request.setCronExpression("0 0 8 * * ?");

        ReminderRuleVO vo = new ReminderRuleVO();
        vo.setId(1L);
        vo.setTitle("提醒");
        vo.setEnabled(true);

        when(lifeService.createReminderRule(any(), eq(1L))).thenReturn(vo);

        mockMvc.perform(post("/api/life/reminder-rules")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("提醒"));
    }

    @Test
    void shouldReturn400WhenReminderRuleCronBlank() throws Exception {
        ReminderRuleCreateRequest request = new ReminderRuleCreateRequest();
        request.setTitle("提醒");
        request.setCronExpression("");

        mockMvc.perform(post("/api/life/reminder-rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldListReminderRules() throws Exception {
        ReminderRuleVO vo = new ReminderRuleVO();
        vo.setId(1L);
        vo.setTitle("规则1");

        when(lifeService.listReminderRules(1L)).thenReturn(List.of(vo));

        mockMvc.perform(get("/api/life/reminder-rules")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("规则1"));
    }

    @Test
    void shouldToggleReminderRuleEnabled() throws Exception {
        ReminderRuleVO vo = new ReminderRuleVO();
        vo.setId(1L);
        vo.setEnabled(false);

        when(lifeService.toggleEnabled(1L, 1L)).thenReturn(vo);

        mockMvc.perform(put("/api/life/reminder-rules/1/toggle")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.enabled").value(false));
    }
}
