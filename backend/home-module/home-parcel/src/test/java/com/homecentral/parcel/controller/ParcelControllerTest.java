package com.homecentral.parcel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homecentral.parcel.api.dto.ParcelCreateRequest;
import com.homecentral.parcel.api.dto.ParcelUpdateRequest;
import com.homecentral.parcel.api.enums.ParcelStatus;
import com.homecentral.parcel.api.vo.ParcelSummaryVO;
import com.homecentral.parcel.api.feign.AuthFeignClient;
import com.homecentral.parcel.service.IParcelService;
import com.homecentral.parcel.tracking.AliyunExpressService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ParcelController.class, properties = {
    "spring.cloud.nacos.config.enabled=false",
    "spring.cloud.nacos.discovery.enabled=false",
    "spring.cloud.config.import-check.enabled=false"
})
class ParcelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private IParcelService parcelService;

    @MockitoBean
    private AliyunExpressService aliyunExpressService;

    @MockitoBean
    private AuthFeignClient authFeignClient;

    private ParcelSummaryVO createVO(Long id) {
        ParcelSummaryVO vo = new ParcelSummaryVO();
        vo.setId(id);
        vo.setCourierCompany("SF");
        vo.setTrackingNumber("SF123");
        vo.setStatus(ParcelStatus.PENDING_PICKUP);
        vo.setCreatedAt(OffsetDateTime.now());
        return vo;
    }

    @Test
    void shouldCreateParcel() throws Exception {
        ParcelCreateRequest request = new ParcelCreateRequest();
        request.setCourierCompany("SF");
        request.setTrackingNumber("SF123");

        when(parcelService.create(any(), eq(1L))).thenReturn(createVO(1L));

        mockMvc.perform(post("/api/parcel")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.courierCompany").value("SF"));
    }

    @Test
    void shouldReturn400WhenCreateRequestInvalid() throws Exception {
        ParcelCreateRequest request = new ParcelCreateRequest();
        request.setCourierCompany("");
        request.setTrackingNumber("");

        mockMvc.perform(post("/api/parcel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetParcelById() throws Exception {
        when(parcelService.getById(1L)).thenReturn(createVO(1L));

        mockMvc.perform(get("/api/parcel/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void shouldListParcelsWithPagination() throws Exception {
        ParcelSummaryVO vo = createVO(1L);
        Page<ParcelSummaryVO> pageResult = new PageImpl<>(List.of(vo), PageRequest.of(0, 20), 1);
        when(parcelService.list(eq(1L), eq("PENDING_PICKUP"), eq("SF"), eq(1), eq(20)))
                .thenReturn(pageResult);

        mockMvc.perform(get("/api/parcel")
                        .header("X-User-Id", 1L)
                        .param("status", "PENDING_PICKUP")
                        .param("trackingNumber", "SF"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].courierCompany").value("SF"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void shouldUpdateParcel() throws Exception {
        ParcelUpdateRequest request = new ParcelUpdateRequest();
        request.setPickupCode("8888");

        ParcelSummaryVO vo = createVO(1L);
        vo.setPickupCode("8888");
        when(parcelService.update(eq(1L), any(), eq(1L))).thenReturn(vo);

        mockMvc.perform(put("/api/parcel/1")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pickupCode").value("8888"));
    }

    @Test
    void shouldPickUpParcel() throws Exception {
        ParcelSummaryVO vo = createVO(1L);
        vo.setStatus(ParcelStatus.PICKED_UP);
        when(parcelService.pickUp(1L, 1L)).thenReturn(vo);

        mockMvc.perform(put("/api/parcel/1/pickup")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PICKED_UP"));
    }

    @Test
    void shouldReceiveParcel() throws Exception {
        ParcelSummaryVO vo = createVO(1L);
        vo.setStatus(ParcelStatus.RECEIVED);
        when(parcelService.receive(1L, 1L)).thenReturn(vo);

        mockMvc.perform(put("/api/parcel/1/receive")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("RECEIVED"));
    }

    @Test
    void shouldDeleteParcel() throws Exception {
        doNothing().when(parcelService).delete(1L, 1L);

        mockMvc.perform(delete("/api/parcel/1")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
