package com.homecentral.fridge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homecentral.fridge.api.dto.FridgeCategoryRequest;
import com.homecentral.fridge.api.dto.FridgeItemRequest;
import com.homecentral.fridge.api.dto.FridgeRecognizeRequest;
import com.homecentral.fridge.api.enums.FridgeItemStatus;
import com.homecentral.fridge.api.enums.FridgeItemSource;
import com.homecentral.fridge.api.enums.FridgeZone;
import com.homecentral.fridge.api.vo.FridgeCategoryVO;
import com.homecentral.fridge.api.vo.FridgeExpiringVO;
import com.homecentral.fridge.api.vo.FridgeItemVO;
import com.homecentral.fridge.api.vo.FridgeRecognizeResult;
import com.homecentral.fridge.api.vo.FridgeRecognizeVO;
import com.homecentral.fridge.service.IFridgeCategoryService;
import com.homecentral.fridge.service.IFridgeItemService;
import com.homecentral.fridge.service.IFridgeRecognizeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = {FridgeCategoryController.class, FridgeItemController.class, FridgeRecognizeController.class, FridgeTemplateController.class},
        properties = {
                "spring.cloud.nacos.config.enabled=false",
                "spring.cloud.nacos.discovery.enabled=false",
                "spring.cloud.config.import-check.enabled=false"
        })
class FridgeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private IFridgeCategoryService categoryService;

    @MockitoBean
    private IFridgeItemService itemService;

    @MockitoBean
    private IFridgeRecognizeService recognizeService;

    @MockitoBean
    private com.homecentral.fridge.service.IFridgeTemplateService templateService;

    private FridgeCategoryVO categoryVO(Long id, String name) {
        FridgeCategoryVO vo = new FridgeCategoryVO();
        vo.setId(id);
        vo.setName(name);
        vo.setIcon("🥬");
        vo.setColor("#10B981");
        vo.setSortOrder(10);
        vo.setSystem(true);
        return vo;
    }

    private FridgeItemVO itemVO(Long id) {
        FridgeItemVO vo = new FridgeItemVO();
        vo.setId(id);
        vo.setName("纯牛奶");
        vo.setZone(FridgeZone.REFRIGERATED);
        vo.setSubZone("冷藏-门上");
        vo.setStatus(FridgeItemStatus.ACTIVE);
        vo.setSource(FridgeItemSource.MANUAL);
        vo.setQuantity(BigDecimal.ONE);
        vo.setUnit("盒");
        return vo;
    }

    @Test
    void shouldListCategories() throws Exception {
        when(categoryService.listVisible(1L)).thenReturn(List.of(categoryVO(1L, "蔬菜")));

        mockMvc.perform(get("/api/fridge/categories").header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("蔬菜"));
    }

    @Test
    void shouldCreateCategory() throws Exception {
        FridgeCategoryRequest req = new FridgeCategoryRequest();
        req.setName("烘焙");
        when(categoryService.create(any(), eq(1L))).thenReturn(categoryVO(2L, "烘焙"));

        mockMvc.perform(post("/api/fridge/categories")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(2));
    }

    @Test
    void shouldReturn400WhenCategoryNameBlank() throws Exception {
        FridgeCategoryRequest req = new FridgeCategoryRequest();
        req.setName("");
        mockMvc.perform(post("/api/fridge/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDeleteCategory() throws Exception {
        doNothing().when(categoryService).delete(1L, 1L);
        mockMvc.perform(delete("/api/fridge/categories/1").header("X-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCreateItem() throws Exception {
        FridgeItemRequest req = new FridgeItemRequest();
        req.setName("纯牛奶");
        req.setZone(FridgeZone.REFRIGERATED);
        when(itemService.create(any(), eq(1L))).thenReturn(itemVO(1L));

        mockMvc.perform(post("/api/fridge/items")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void shouldReturn400WhenItemNameBlank() throws Exception {
        FridgeItemRequest req = new FridgeItemRequest();
        req.setName("");
        req.setZone(FridgeZone.REFRIGERATED);
        mockMvc.perform(post("/api/fridge/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldListItems() throws Exception {
        when(itemService.list(eq(1L), eq(FridgeZone.REFRIGERATED), eq(1L), eq(FridgeItemStatus.ACTIVE), eq(false)))
                .thenReturn(List.of(itemVO(1L)));

        mockMvc.perform(get("/api/fridge/items")
                        .header("X-User-Id", 1L)
                        .param("zone", "REFRIGERATED")
                        .param("categoryId", "1")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("纯牛奶"));
    }

    @Test
    void shouldListItemsIncludingPending() throws Exception {
        when(itemService.list(eq(1L), eq(null), eq(null), eq(null), eq(true)))
                .thenReturn(List.of(itemVO(1L)));

        mockMvc.perform(get("/api/fridge/items")
                        .header("X-User-Id", 1L)
                        .param("includePending", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("纯牛奶"));
    }

    @Test
    void shouldMoveItemToZone() throws Exception {
        FridgeItemVO moved = itemVO(1L);
        moved.setZone(FridgeZone.FROZEN);
        moved.setSubZone("FROZEN-L1");
        moved.setStatus(FridgeItemStatus.ACTIVE);
        when(itemService.move(eq(1L), any(), eq(1L))).thenReturn(moved);

        String body = "{\"zone\":\"FROZEN\",\"subZone\":\"FROZEN-L1\"}";
        mockMvc.perform(post("/api/fridge/items/1/move")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.zone").value("FROZEN"))
                .andExpect(jsonPath("$.data.subZone").value("FROZEN-L1"));
    }

    @Test
    void shouldMoveItemBackToBasketWithEmptyBody() throws Exception {
        FridgeItemVO back = itemVO(1L);
        back.setStatus(FridgeItemStatus.PENDING);
        back.setZone(null);
        back.setSubZone(null);
        when(itemService.move(eq(1L), any(), eq(1L))).thenReturn(back);

        mockMvc.perform(post("/api/fridge/items/1/move")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void shouldQuickCreateItem() throws Exception {
        FridgeItemVO vo = itemVO(10L);
        vo.setStatus(FridgeItemStatus.PENDING);
        when(itemService.quickCreate(any(), eq(1L))).thenReturn(vo);

        String body = "{\"name\":\"鸡蛋\",\"categoryId\":1}";
        mockMvc.perform(post("/api/fridge/items/quick")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void shouldReturn400WhenQuickCreateNameBlank() throws Exception {
        String body = "{\"name\":\"\"}";
        mockMvc.perform(post("/api/fridge/items/quick")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetItemById() throws Exception {
        when(itemService.getById(1L, 1L)).thenReturn(itemVO(1L));
        mockMvc.perform(get("/api/fridge/items/1").header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void shouldConsumeItem() throws Exception {
        FridgeItemVO vo = itemVO(1L);
        vo.setStatus(FridgeItemStatus.CONSUMED);
        when(itemService.consume(1L, 1L)).thenReturn(vo);
        mockMvc.perform(post("/api/fridge/items/1/consume").header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONSUMED"));
    }

    @Test
    void shouldConsumeOneItem() throws Exception {
        FridgeItemVO vo = itemVO(1L);
        vo.setStatus(FridgeItemStatus.ACTIVE);
        vo.setQuantity(new java.math.BigDecimal(2));
        when(itemService.consumeOne(1L, 1L)).thenReturn(vo);
        mockMvc.perform(post("/api/fridge/items/1/consume-one").header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.quantity").value(2));
    }

    @Test
    void shouldDiscardItem() throws Exception {
        FridgeItemVO vo = itemVO(1L);
        vo.setStatus(FridgeItemStatus.DISCARDED);
        when(itemService.discard(1L, 1L)).thenReturn(vo);
        mockMvc.perform(post("/api/fridge/items/1/discard").header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DISCARDED"));
    }

    @Test
    void shouldDeleteItem() throws Exception {
        doNothing().when(itemService).delete(1L, 1L);
        mockMvc.perform(delete("/api/fridge/items/1").header("X-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetExpiringStats() throws Exception {
        when(itemService.expiringStats(1L, 7)).thenReturn(new FridgeExpiringVO(2L, 5L, 7));
        mockMvc.perform(get("/api/fridge/items/expiring")
                        .header("X-User-Id", 1L)
                        .param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.expiredCount").value(2))
                .andExpect(jsonPath("$.data.expiringCount").value(5))
                .andExpect(jsonPath("$.data.thresholdDays").value(7));
    }

    @Test
    void shouldBatchCreateItems() throws Exception {
        FridgeItemRequest a = new FridgeItemRequest();
        a.setName("鸡蛋");
        a.setZone(FridgeZone.REFRIGERATED);
        FridgeItemRequest b = new FridgeItemRequest();
        b.setName("西红柿");
        b.setZone(FridgeZone.REFRIGERATED);
        when(itemService.batchCreate(any(), eq(1L))).thenReturn(List.of(itemVO(1L), itemVO(2L)));

        mockMvc.perform(post("/api/fridge/items/batch")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(a, b))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void shouldRecognizeItems() throws Exception {
        FridgeRecognizeRequest req = new FridgeRecognizeRequest();
        req.setImageBase64("aGVsbG8=");

        FridgeRecognizeVO vo = new FridgeRecognizeVO();
        vo.setName("纯牛奶");
        vo.setSuggestedZone(FridgeZone.REFRIGERATED);
        when(recognizeService.recognize(any())).thenReturn(new FridgeRecognizeResult(List.of(vo)));

        mockMvc.perform(post("/api/fridge/recognize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.items[0].name").value("纯牛奶"));
    }

    @Test
    void shouldReturnErrorWhenRecognizeMissingImage() throws Exception {
        FridgeRecognizeRequest req = new FridgeRecognizeRequest();
        mockMvc.perform(post("/api/fridge/recognize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ---- FridgeTemplateController ----

    private com.homecentral.fridge.api.vo.FridgeTemplateVO templateVO(Long id, String name,
                                                                       com.homecentral.fridge.api.enums.FridgeLayout layout,
                                                                       boolean isDefault, boolean isSystem) {
        com.homecentral.fridge.api.vo.FridgeTemplateVO vo = new com.homecentral.fridge.api.vo.FridgeTemplateVO();
        vo.setId(id);
        vo.setName(name);
        vo.setLayout(layout);
        vo.setFridgeLayers(3);
        vo.setFreezerLayers(2);
        vo.setChillerLayers(0);
        vo.setDoorShelfCount(3);
        vo.setDefault(isDefault);
        vo.setSystem(isSystem);
        return vo;
    }

    @Test
    void shouldListTemplates() throws Exception {
        when(templateService.listVisible(1L))
                .thenReturn(List.of(templateVO(1L, "经典单开", com.homecentral.fridge.api.enums.FridgeLayout.CLASSIC, false, true)));

        mockMvc.perform(get("/api/fridge/templates").header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("经典单开"));
    }

    @Test
    void shouldGetDefaultTemplate() throws Exception {
        when(templateService.getDefault(1L))
                .thenReturn(templateVO(2L, "对开门", com.homecentral.fridge.api.enums.FridgeLayout.SIDE_BY_SIDE, true, false));

        mockMvc.perform(get("/api/fridge/templates/default").header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.layout").value("SIDE_BY_SIDE"))
                .andExpect(jsonPath("$.data.default").value(true));
    }

    @Test
    void shouldCreateTemplate() throws Exception {
        com.homecentral.fridge.api.dto.FridgeTemplateRequest req = new com.homecentral.fridge.api.dto.FridgeTemplateRequest();
        req.setName("我家冰箱");
        req.setLayout(com.homecentral.fridge.api.enums.FridgeLayout.CLASSIC);
        req.setFridgeLayers(3);
        req.setFreezerLayers(2);
        when(templateService.create(any(), eq(1L)))
                .thenReturn(templateVO(10L, "我家冰箱", com.homecentral.fridge.api.enums.FridgeLayout.CLASSIC, false, false));

        mockMvc.perform(post("/api/fridge/templates")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10));
    }

    @Test
    void shouldReturn400WhenTemplateNameBlank() throws Exception {
        com.homecentral.fridge.api.dto.FridgeTemplateRequest req = new com.homecentral.fridge.api.dto.FridgeTemplateRequest();
        req.setName("");
        req.setLayout(com.homecentral.fridge.api.enums.FridgeLayout.CLASSIC);
        mockMvc.perform(post("/api/fridge/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateTemplate() throws Exception {
        com.homecentral.fridge.api.dto.FridgeTemplateRequest req = new com.homecentral.fridge.api.dto.FridgeTemplateRequest();
        req.setName("我家冰箱V2");
        req.setLayout(com.homecentral.fridge.api.enums.FridgeLayout.BOTTOM_FREEZER);
        when(templateService.update(eq(10L), any(), eq(1L)))
                .thenReturn(templateVO(10L, "我家冰箱V2", com.homecentral.fridge.api.enums.FridgeLayout.BOTTOM_FREEZER, false, false));

        mockMvc.perform(put("/api/fridge/templates/10")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.layout").value("BOTTOM_FREEZER"));
    }

    @Test
    void shouldDeleteTemplate() throws Exception {
        doNothing().when(templateService).delete(10L, 1L);
        mockMvc.perform(delete("/api/fridge/templates/10").header("X-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void shouldActivateTemplate() throws Exception {
        when(templateService.activate(eq(1L), eq(1L)))
                .thenReturn(templateVO(1L, "经典单开", com.homecentral.fridge.api.enums.FridgeLayout.CLASSIC, true, false));

        mockMvc.perform(post("/api/fridge/templates/1/activate").header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.default").value(true));
    }
}
