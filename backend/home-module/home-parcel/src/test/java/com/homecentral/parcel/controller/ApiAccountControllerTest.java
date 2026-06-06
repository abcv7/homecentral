package com.homecentral.parcel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homecentral.parcel.api.dto.ApiAccountRequest;
import com.homecentral.parcel.api.vo.ApiAccountVO;
import com.homecentral.parcel.service.IApiAccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = ApiAccountController.class, properties = {
    "spring.cloud.nacos.config.enabled=false",
    "spring.cloud.nacos.discovery.enabled=false",
    "spring.cloud.config.import-check.enabled=false"
})
class ApiAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private IApiAccountService apiAccountService;

    private ApiAccountVO createVO() {
        ApiAccountVO vo = new ApiAccountVO();
        vo.setId(1L);
        vo.setUserId(1L);
        vo.setProvider("ALIYUN_EXPRESS");
        vo.setApiKeyMasked("****1234");
        vo.setCustomer("test-customer");
        vo.setEnabled(true);
        return vo;
    }

    @Test
    void shouldGetApiAccount() throws Exception {
        when(apiAccountService.get(1L, "ALIYUN_EXPRESS")).thenReturn(createVO());

        mockMvc.perform(get("/api/parcel/api-account/ALIYUN_EXPRESS")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.apiKeyMasked").value("****1234"))
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    @Test
    void shouldReturnNullWhenGetWithoutUserId() throws Exception {
        mockMvc.perform(get("/api/parcel/api-account/ALIYUN_EXPRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
        verify(apiAccountService, never()).get(any(), any());
    }

    @Test
    void shouldReturnNullInDataWhenAccountDoesNotExist() throws Exception {
        when(apiAccountService.get(1L, "ALIYUN_EXPRESS")).thenReturn(null);

        mockMvc.perform(get("/api/parcel/api-account/ALIYUN_EXPRESS")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void shouldSaveApiAccount() throws Exception {
        ApiAccountRequest request = new ApiAccountRequest();
        request.setApiKey("new-key-1234");
        request.setCustomer("test-customer");
        request.setEnabled(true);

        when(apiAccountService.save(eq(1L), eq("ALIYUN_EXPRESS"), any())).thenReturn(createVO());

        mockMvc.perform(put("/api/parcel/api-account/ALIYUN_EXPRESS")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.apiKeyMasked").value("****1234"));
    }

    @Test
    void shouldReturnFailWhenSaveWithoutUserId() throws Exception {
        ApiAccountRequest request = new ApiAccountRequest();
        request.setApiKey("new-key-1234");

        mockMvc.perform(put("/api/parcel/api-account/ALIYUN_EXPRESS")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("未登录"));
        verify(apiAccountService, never()).save(any(), any(), any());
    }

    @Test
    void shouldDeleteApiAccount() throws Exception {
        doNothing().when(apiAccountService).delete(1L, "ALIYUN_EXPRESS");

        mockMvc.perform(delete("/api/parcel/api-account/ALIYUN_EXPRESS")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(apiAccountService).delete(1L, "ALIYUN_EXPRESS");
    }

    @Test
    void shouldReturnFailWhenDeleteWithoutUserId() throws Exception {
        mockMvc.perform(delete("/api/parcel/api-account/ALIYUN_EXPRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("未登录"));
        verify(apiAccountService, never()).delete(any(), any());
    }
}
