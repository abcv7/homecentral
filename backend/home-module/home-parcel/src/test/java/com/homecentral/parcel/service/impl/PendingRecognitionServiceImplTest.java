package com.homecentral.parcel.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.homecentral.common.model.BatchResult;
import com.homecentral.parcel.api.vo.ParcelRecognitionResult;
import com.homecentral.parcel.api.vo.ParcelRecognitionVO;
import com.homecentral.parcel.api.vo.ParcelSummaryVO;
import com.homecentral.parcel.api.vo.PendingRecognitionVO;
import com.homecentral.parcel.entity.PendingRecognition;
import com.homecentral.parcel.mapper.PendingRecognitionMapper;
import com.homecentral.parcel.service.IParcelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PendingRecognitionServiceImplTest {

    @Mock
    private PendingRecognitionMapper pendingMapper;

    @Mock
    private IParcelService parcelService;

    private ObjectMapper objectMapper;
    private PendingRecognitionServiceImpl service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        service = new PendingRecognitionServiceImpl(pendingMapper, parcelService, objectMapper);
    }

    @Test
    void shouldCreateProcessingAndReturnId() {
        when(pendingMapper.insert(any(PendingRecognition.class))).thenAnswer(inv -> {
            PendingRecognition p = inv.getArgument(0);
            p.setId(42L);
            return 1;
        });

        Long id = service.createProcessing(7L, "hash-abc");

        assertEquals(42L, id);
        ArgumentCaptor<PendingRecognition> captor = ArgumentCaptor.forClass(PendingRecognition.class);
        verify(pendingMapper).insert(captor.capture());
        PendingRecognition p = captor.getValue();
        assertEquals(7L, p.getUserId());
        assertEquals("processing", p.getStatus());
        assertEquals("hash-abc", p.getInputHash());
        assertNotNull(p.getCreatedAt());
    }

    @Test
    void shouldMarkCompletedWithExpiresAt() {
        when(pendingMapper.updateById(any(PendingRecognition.class))).thenReturn(1);

        ParcelRecognitionResult result = new ParcelRecognitionResult();
        result.setParcels(List.of(buildVO("SF", "T1", "P1")));

        service.markCompleted(1L, result);

        ArgumentCaptor<PendingRecognition> captor = ArgumentCaptor.forClass(PendingRecognition.class);
        verify(pendingMapper).updateById(captor.capture());
        PendingRecognition p = captor.getValue();
        assertEquals("completed", p.getStatus());
        assertNotNull(p.getResultJson());
        assertNotNull(p.getExpiresAt());
        assertNotNull(p.getCompletedAt());
    }

    @Test
    void shouldMarkFailedWithTruncatedMessage() {
        when(pendingMapper.updateById(any(PendingRecognition.class))).thenReturn(1);

        String longMsg = "x".repeat(2000);
        service.markFailed(1L, longMsg);

        ArgumentCaptor<PendingRecognition> captor = ArgumentCaptor.forClass(PendingRecognition.class);
        verify(pendingMapper).updateById(captor.capture());
        PendingRecognition p = captor.getValue();
        assertEquals("failed", p.getStatus());
        assertEquals(1000, p.getFailureMessage().length());
    }

    @Test
    void shouldAutoImportExpiredCompletedRecords() throws Exception {
        PendingRecognition expired = buildPending(1L, 1L, "completed");
        expired.setResultJson(objectMapper.writeValueAsString(buildResult("SF", "T1", "P1")));
        when(pendingMapper.selectList(any())).thenReturn(List.of(expired));
        when(parcelService.batchImport(anyList(), anyLong())).thenReturn(BatchResult.of(List.of(), List.of()));

        int count = service.autoImportExpired();

        assertEquals(1, count);
        verify(parcelService).batchImport(anyList(), eq(1L));

        ArgumentCaptor<PendingRecognition> captor = ArgumentCaptor.forClass(PendingRecognition.class);
        verify(pendingMapper).updateById(captor.capture());
        assertEquals("imported", captor.getValue().getStatus());
    }

    @Test
    void shouldRecordFailuresOnPartialImport() throws Exception {
        PendingRecognition p = buildPending(2L, 1L, "completed");
        p.setResultJson(objectMapper.writeValueAsString(buildResult("SF", "T1", "P1")));
        when(pendingMapper.selectList(any())).thenReturn(List.of(p));

        BatchResult.FailureItem fi = new BatchResult.FailureItem();
        fi.setIndex(0);
        fi.setItem("T1");
        fi.setReason("运单号重复");
        BatchResult<ParcelSummaryVO> br = BatchResult.of(List.of(), List.of(fi));
        when(parcelService.batchImport(anyList(), anyLong())).thenReturn(br);

        int count = service.autoImportExpired();

        assertEquals(1, count);

        ArgumentCaptor<PendingRecognition> captor = ArgumentCaptor.forClass(PendingRecognition.class);
        verify(pendingMapper).updateById(captor.capture());
        PendingRecognition updated = captor.getValue();
        assertEquals("imported", updated.getStatus());
        assertNotNull(updated.getFailureMessage());
        assertTrue(updated.getFailureMessage().contains("运单号重复"));
    }

    @Test
    void shouldMarkProcessingAsTimeout() {
        PendingRecognition stale = buildPending(5L, 1L, "processing");
        when(pendingMapper.selectList(any())).thenReturn(List.of(stale));

        int count = service.markProcessingTimeout();

        assertEquals(1, count);
        ArgumentCaptor<PendingRecognition> captor = ArgumentCaptor.forClass(PendingRecognition.class);
        verify(pendingMapper).updateById(captor.capture());
        PendingRecognition updated = captor.getValue();
        assertEquals("failed", updated.getStatus());
        assertTrue(updated.getFailureMessage().contains("超时"));
    }

    @Test
    void shouldReturnNullForUnknownPending() {
        when(pendingMapper.selectById(99L)).thenReturn(null);

        assertNull(service.getById(99L, 1L));
    }

    @Test
    void shouldReturnNullWhenPendingBelongsToOtherUser() {
        PendingRecognition p = buildPending(1L, 99L, "completed");
        when(pendingMapper.selectById(1L)).thenReturn(p);

        assertNull(service.getById(1L, 1L));
    }

    @Test
    void shouldListPendingFilteringByUserAndActiveStatus() {
        PendingRecognition p1 = buildPending(1L, 7L, "processing");
        when(pendingMapper.selectList(any())).thenReturn(List.of(p1));

        List<PendingRecognitionVO> result = service.listPending(7L);

        assertEquals(1, result.size());
        assertEquals("processing", result.get(0).getStatus());
    }

    @Test
    void shouldScheduledScanInvokeAllSteps() {
        when(pendingMapper.selectList(any())).thenReturn(List.of());
        when(pendingMapper.delete(any())).thenReturn(0);

        service.scheduledScan();

        verify(pendingMapper, atLeastOnce()).selectList(any());
    }

    private PendingRecognition buildPending(long id, long userId, String status) {
        PendingRecognition p = new PendingRecognition();
        p.setId(id);
        p.setUserId(userId);
        p.setStatus(status);
        p.setCreatedAt(OffsetDateTime.now());
        return p;
    }

    private ParcelRecognitionVO buildVO(String company, String tracking, String code) {
        ParcelRecognitionVO vo = new ParcelRecognitionVO();
        vo.setCourierCompany(company);
        vo.setTrackingNumber(tracking);
        vo.setPickupCode(code);
        vo.setArrivedDate(LocalDate.now());
        return vo;
    }

    private ParcelRecognitionResult buildResult(String company, String tracking, String code) {
        ParcelRecognitionResult r = new ParcelRecognitionResult();
        r.setParcels(List.of(buildVO(company, tracking, code)));
        return r;
    }
}
