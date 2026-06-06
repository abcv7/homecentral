package com.homecentral.parcel.service.impl;

import com.homecentral.common.model.BatchResult;
import com.homecentral.common.model.Result;
import com.homecentral.friend.api.feign.FriendClient;
import com.homecentral.parcel.api.dto.ParcelCreateRequest;
import com.homecentral.parcel.api.dto.ParcelUpdateRequest;
import com.homecentral.parcel.api.enums.ParcelStatus;
import com.homecentral.parcel.api.feign.AuthFeignClient;
import com.homecentral.parcel.api.vo.ParcelSummaryVO;
import com.homecentral.parcel.api.vo.SharedParcelUserVO;
import com.homecentral.parcel.api.vo.TrackingVO;
import com.homecentral.parcel.entity.Parcel;
import com.homecentral.parcel.entity.ParcelShare;
import com.homecentral.parcel.mapper.ParcelMapper;
import com.homecentral.parcel.mapper.ParcelShareMapper;
import com.homecentral.parcel.tracking.AliyunExpressService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;

import java.util.List;

import com.homecentral.auth.api.vo.MemberVO;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParcelServiceImplTest {

    @Mock
    private ParcelMapper parcelMapper;

    @Mock
    private ParcelShareMapper parcelShareMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private AliyunExpressService aliyunExpressService;

    @Mock
    private AuthFeignClient authFeignClient;

    @Mock
    private FriendClient friendClient;

    @InjectMocks
    private ParcelServiceImpl parcelService;

    private Parcel createParcel(Long id, String status) {
        Parcel parcel = new Parcel();
        parcel.setId(id);
        parcel.setCourierCompany("SF");
        parcel.setTrackingNumber("SF123456");
        parcel.setStatus(status);
        parcel.setSource("MANUAL");
        return parcel;
    }

    @Test
    void shouldCreateParcelWithPendingStatus() {
        ParcelCreateRequest request = new ParcelCreateRequest();
        request.setCourierCompany("韵达快递");
        request.setTrackingNumber("YD789012");

        ArgumentCaptor<Parcel> captor = ArgumentCaptor.forClass(Parcel.class);
        when(parcelMapper.insert(captor.capture())).thenAnswer(invocation -> {
            Parcel p = invocation.getArgument(0);
            p.setId(1L);
            return 1;
        });

        ParcelSummaryVO vo = parcelService.create(request, 1L);

        assertNotNull(vo);
        assertEquals(ParcelStatus.PENDING_PICKUP, vo.getStatus());
        assertEquals("韵达快递", vo.getCourierCompany());

        Parcel captured = captor.getValue();
        assertEquals("PENDING_PICKUP", captured.getStatus());
        assertEquals(1L, captured.getCreatedBy());
    }

    @Test
    void shouldReturnParcelWhenGetByIdExists() {
        Parcel parcel = createParcel(1L, "PENDING_PICKUP");
        when(parcelMapper.selectById(1L)).thenReturn(parcel);

        ParcelSummaryVO vo = parcelService.getById(1L);

        assertNotNull(vo);
        assertEquals(1L, vo.getId());
        assertEquals("SF", vo.getCourierCompany());
    }

    @Test
    void shouldThrowWhenGetByIdNotFound() {
        when(parcelMapper.selectById(999L)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> parcelService.getById(999L));
        assertEquals("快递不存在", ex.getMessage());
    }

    @Test
    void shouldListByUserIdAndStatusAndTrackingNumber() {
        Parcel parcel = createParcel(1L, "PENDING_PICKUP");
        when(parcelMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<Parcel> mpPage = invocation.getArgument(0);
            mpPage.setRecords(List.of(parcel));
            mpPage.setTotal(1);
            return mpPage;
        });

        Page<ParcelSummaryVO> result = parcelService.list(1L, "PENDING_PICKUP", "SF123", 1, 20);

        assertEquals(1, result.getTotalElements());
        assertEquals("SF", result.getContent().getFirst().getCourierCompany());
    }

    @Test
    void shouldReturnEmptyListWhenNoParcels() {
        when(parcelMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<Parcel> mpPage = invocation.getArgument(0);
            mpPage.setRecords(List.of());
            mpPage.setTotal(0);
            return mpPage;
        });

        Page<ParcelSummaryVO> result = parcelService.list(null, null, null, 1, 20);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldUpdateParcelWhenPending() {
        Parcel parcel = createParcel(1L, "PENDING_PICKUP");
        when(parcelMapper.selectById(1L)).thenReturn(parcel);

        ParcelUpdateRequest request = new ParcelUpdateRequest();
        request.setPickupCode("6666");

        ParcelSummaryVO vo = parcelService.update(1L, request, 1L);

        assertEquals("6666", vo.getPickupCode());
        verify(parcelMapper).updateById(parcel);
    }

    @Test
    void shouldThrowWhenUpdateNonExistent() {
        when(parcelMapper.selectById(999L)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> parcelService.update(999L, new ParcelUpdateRequest(), 1L));
        assertEquals("快递不存在", ex.getMessage());
    }

    @Test
    void shouldThrowWhenUpdateAlreadyPickedUp() {
        Parcel parcel = createParcel(1L, "PICKED_UP");
        when(parcelMapper.selectById(1L)).thenReturn(parcel);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> parcelService.update(1L, new ParcelUpdateRequest(), 1L));
        assertEquals("已取件的快递不能修改", ex.getMessage());
    }

    @Test
    void shouldPickUpSuccess() {
        Parcel parcel = createParcel(1L, "PENDING_PICKUP");
        when(parcelMapper.selectById(1L)).thenReturn(parcel);

        ParcelSummaryVO vo = parcelService.pickUp(1L, 1L);

        assertEquals(ParcelStatus.PICKED_UP, vo.getStatus());
        verify(parcelMapper).updateById(parcel);
    }

    @Test
    void shouldThrowWhenPickUpAlreadyPicked() {
        Parcel parcel = createParcel(1L, "PICKED_UP");
        when(parcelMapper.selectById(1L)).thenReturn(parcel);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> parcelService.pickUp(1L, 1L));
        assertEquals("快递已被取件", ex.getMessage());
    }

    @Test
    void shouldDeleteSuccessWhenReceived() {
        Parcel parcel = createParcel(1L, "RECEIVED");
        when(parcelMapper.selectById(1L)).thenReturn(parcel);

        parcelService.delete(1L, 1L);

        verify(parcelMapper).deleteById(1L);
    }

    @Test
    void shouldReceiveSuccess() {
        Parcel parcel = createParcel(1L, "PICKED_UP");
        when(parcelMapper.selectById(1L)).thenReturn(parcel);

        ParcelSummaryVO vo = parcelService.receive(1L, 1L);

        assertEquals(ParcelStatus.RECEIVED, vo.getStatus());
        verify(parcelMapper).updateById(parcel);
    }

    @Test
    void shouldThrowWhenReceiveNotPickedUp() {
        Parcel parcel = createParcel(1L, "PENDING_PICKUP");
        when(parcelMapper.selectById(1L)).thenReturn(parcel);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> parcelService.receive(1L, 1L));
        assertEquals("快递状态不是已取件", ex.getMessage());
    }

    @Test
    void shouldThrowWhenDeleteNonExistent() {
        when(parcelMapper.selectById(999L)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> parcelService.delete(999L, 1L));
        assertEquals("快递不存在", ex.getMessage());
    }

    @Test
    void shouldThrowWhenDeleteNotReceived() {
        Parcel parcel = createParcel(1L, "PICKED_UP");
        when(parcelMapper.selectById(1L)).thenReturn(parcel);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> parcelService.delete(1L, 1L));
        assertEquals("仅已收货的快递可以删除", ex.getMessage());
    }

    @Test
    void shouldThrowWhenDeletePending() {
        Parcel parcel = createParcel(1L, "PENDING_PICKUP");
        when(parcelMapper.selectById(1L)).thenReturn(parcel);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> parcelService.delete(1L, 1L));
        assertEquals("仅已收货的快递可以删除", ex.getMessage());
    }

    @Test
    void shouldBatchImportSuccessWhenNoConflict() {
        when(parcelMapper.selectCount(any())).thenReturn(0L);
        when(parcelMapper.insert(any(Parcel.class))).thenAnswer(inv -> {
            Parcel p = inv.getArgument(0);
            p.setId(System.nanoTime());
            return 1;
        });

        ParcelCreateRequest req = new ParcelCreateRequest();
        req.setCourierCompany("顺丰");
        req.setTrackingNumber("SF123");
        req.setPickupCode("8888");

        BatchResult<ParcelSummaryVO> result = parcelService.batchImport(List.of(req), 1L);

        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void shouldBatchImportFailWhenDuplicateTrackingNumber() {
        when(parcelMapper.selectCount(any()))
            .thenReturn(1L)
            .thenReturn(0L);

        ParcelCreateRequest req = new ParcelCreateRequest();
        req.setCourierCompany("顺丰");
        req.setTrackingNumber("SF123");
        req.setPickupCode("8888");

        BatchResult<ParcelSummaryVO> result = parcelService.batchImport(List.of(req), 1L);

        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        assertTrue(result.getFailureItems().get(0).getReason().contains("运单号"));
    }

    @Test
    void shouldBatchImportFailWhenDuplicatePickupCodeRegardlessOfStatus() {
        when(parcelMapper.selectCount(any()))
            .thenReturn(0L)
            .thenReturn(1L);

        ParcelCreateRequest req = new ParcelCreateRequest();
        req.setCourierCompany("顺丰");
        req.setTrackingNumber("SF999");
        req.setPickupCode("8888");

        BatchResult<ParcelSummaryVO> result = parcelService.batchImport(List.of(req), 1L);

        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        assertTrue(result.getFailureItems().get(0).getReason().contains("取件码"));
    }

    @Test
    void shouldReturnPlaceholderWhenTrackingNumberIsChineseUnknown() {
        Parcel parcel = createParcel(1L, "PENDING_PICKUP");
        parcel.setTrackingNumber("未知");
        when(parcelMapper.selectById(1L)).thenReturn(parcel);

        TrackingVO vo = parcelService.queryTracking(1L);

        assertNotNull(vo);
        assertEquals("未知", vo.getTrackingNumber());
        assertEquals("no_tracking_number", vo.getState());
        assertEquals("暂无具体单号", vo.getStateLabel());
        assertEquals("该包裹尚未提供具体运单号", vo.getMessage());
        assertTrue(vo.getTracks() == null || vo.getTracks().isEmpty());
        verifyNoInteractions(aliyunExpressService);
        verifyNoInteractions(authFeignClient);
    }

    @Test
    void shouldReturnPlaceholderWhenTrackingNumberIsEnglishUnknown() {
        Parcel parcel = createParcel(1L, "PENDING_PICKUP");
        parcel.setTrackingNumber("Unknown");
        when(parcelMapper.selectById(1L)).thenReturn(parcel);

        TrackingVO vo = parcelService.queryTracking(1L);

        assertNotNull(vo);
        assertEquals("no_tracking_number", vo.getState());
        assertEquals("暂无具体单号", vo.getStateLabel());
        verifyNoInteractions(aliyunExpressService);
    }

    @Test
    void shouldReturnPlaceholderWhenTrackingNumberIsEmpty() {
        Parcel parcel = createParcel(1L, "PENDING_PICKUP");
        parcel.setTrackingNumber("");
        when(parcelMapper.selectById(1L)).thenReturn(parcel);

        TrackingVO vo = parcelService.queryTracking(1L);

        assertNotNull(vo);
        assertEquals("no_tracking_number", vo.getState());
        assertEquals("暂无具体单号", vo.getStateLabel());
        verifyNoInteractions(aliyunExpressService);
    }

    @Test
    void shouldQueryAliyunForValidTrackingNumber() {
        Parcel parcel = createParcel(1L, "PENDING_PICKUP");
        parcel.setTrackingNumber("SF1234567890");
        when(parcelMapper.selectById(1L)).thenReturn(parcel);
        when(aliyunExpressService.queryWithDiscern("SF1234567890", null))
                .thenReturn(new TrackingVO());

        parcelService.queryTracking(1L);

        verify(aliyunExpressService).queryWithDiscern("SF1234567890", null);
    }

    @Test
    void shouldListSharedUsersWithHydratedMemberInfo() {
        Parcel parcel = createParcel(1L, "PENDING_PICKUP");
        parcel.setCreatedBy(1L);
        parcel.setOwnerUserId(1L);
        when(parcelMapper.selectById(1L)).thenReturn(parcel);

        ParcelShare s1 = new ParcelShare();
        s1.setParcelId(1L);
        s1.setSharedWithUserId(2L);
        ParcelShare s2 = new ParcelShare();
        s2.setParcelId(1L);
        s2.setSharedWithUserId(3L);
        when(parcelShareMapper.selectList(any())).thenReturn(List.of(s1, s2));

        MemberVO m2 = new MemberVO();
        m2.setId(2L);
        m2.setNickname("Alice");
        m2.setEmail("alice@example.com");
        MemberVO m3 = new MemberVO();
        m3.setId(3L);
        m3.setNickname("Bob");
        m3.setEmail("bob@example.com");
        when(authFeignClient.getMemberById(2L))
                .thenReturn(Result.ok(m2));
        when(authFeignClient.getMemberById(3L))
                .thenReturn(Result.ok(m3));

        List<SharedParcelUserVO> result = parcelService.listSharedUsers(1L, 1L);

        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).getUserId());
        assertEquals("Alice", result.get(0).getNickname());
        assertEquals("alice@example.com", result.get(0).getEmail());
        assertEquals(3L, result.get(1).getUserId());
        assertEquals("Bob", result.get(1).getNickname());
    }

    @Test
    void shouldThrowWhenListSharedUsersByNonOwner() {
        Parcel parcel = createParcel(1L, "PENDING_PICKUP");
        parcel.setCreatedBy(1L);
        parcel.setOwnerUserId(1L);
        when(parcelMapper.selectById(1L)).thenReturn(parcel);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> parcelService.listSharedUsers(1L, 99L));
        assertEquals("无权查看此快递的分享列表", ex.getMessage());
        verify(parcelShareMapper, never()).selectList(any());
    }

    @Test
    void shouldThrowWhenListSharedUsersParcelMissing() {
        when(parcelMapper.selectById(404L)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> parcelService.listSharedUsers(404L, 1L));
        assertEquals("快递不存在", ex.getMessage());
    }

    @Test
    void shouldPopulateSharedCountInGetById() {
        Parcel parcel = createParcel(1L, "PENDING_PICKUP");
        parcel.setCreatedBy(1L);
        when(parcelMapper.selectById(1L)).thenReturn(parcel);
        when(parcelShareMapper.selectCount(any())).thenReturn(3L);

        ParcelSummaryVO vo = parcelService.getById(1L);

        assertEquals(3, vo.getSharedCount());
    }

    @Test
    void shouldBatchQueryShareCountInList() {
        Parcel p1 = createParcel(1L, "PENDING_PICKUP");
        Parcel p2 = createParcel(2L, "PENDING_PICKUP");
        when(parcelMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<Parcel> mpPage = invocation.getArgument(0);
            mpPage.setRecords(List.of(p1, p2));
            mpPage.setTotal(2);
            return mpPage;
        });
        ParcelShare s1 = new ParcelShare();
        s1.setParcelId(1L);
        s1.setSharedWithUserId(10L);
        ParcelShare s2 = new ParcelShare();
        s2.setParcelId(1L);
        s2.setSharedWithUserId(11L);
        ParcelShare s3 = new ParcelShare();
        s3.setParcelId(2L);
        s3.setSharedWithUserId(20L);
        when(parcelShareMapper.selectList(any())).thenReturn(List.of(s1, s2, s3));

        Page<ParcelSummaryVO> result = parcelService.list(1L, null, null, 1, 20);

        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getContent().get(0).getSharedCount());
        assertEquals(1, result.getContent().get(1).getSharedCount());
    }
}
