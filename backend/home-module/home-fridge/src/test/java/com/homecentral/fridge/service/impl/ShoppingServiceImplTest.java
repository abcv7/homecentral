package com.homecentral.fridge.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.homecentral.auth.api.feign.MemberEmailClient;
import com.homecentral.common.model.Result;
import com.homecentral.fridge.api.dto.FridgeShoppingConfirmRequest;
import com.homecentral.fridge.api.dto.FridgeShoppingItem;
import com.homecentral.fridge.api.vo.FridgeShoppingHistoryVO;
import com.homecentral.fridge.entity.FridgeShoppingHistory;
import com.homecentral.fridge.mapper.FridgeItemMapper;
import com.homecentral.fridge.mapper.FridgeShoppingHistoryMapper;
import com.homecentral.friend.api.feign.FriendClient;
import com.homecentral.notification.api.email.PurchaseNoticeEmail;
import com.homecentral.notification.api.email.PurchaseNoticeEmail.Variant;
import com.homecentral.notification.api.feign.MailClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingServiceImplTest {

    @Mock
    private FridgeShoppingHistoryMapper historyMapper;

    @Mock
    private FridgeItemMapper itemMapper;

    @Mock
    private FriendClient friendClient;

    @Mock
    private MemberEmailClient memberEmailClient;

    @Mock
    private MailClient mailClient;

    @InjectMocks
    private ShoppingServiceImpl service;

    private FridgeShoppingItem item(String name) {
        FridgeShoppingItem it = new FridgeShoppingItem();
        it.setName(name);
        it.setQuantity(BigDecimal.ONE);
        it.setSource("MANUAL");
        return it;
    }

    @Test
    void shouldConfirmPurchaseWriteBatch() {
        FridgeShoppingConfirmRequest req = new FridgeShoppingConfirmRequest();
        req.setItems(List.of(item("鸡蛋"), item("西红柿")));
        req.setPartnerEmail("partner@example.com");

        ArgumentCaptor<FridgeShoppingHistory> captor = ArgumentCaptor.forClass(FridgeShoppingHistory.class);
        when(historyMapper.insert(captor.capture())).thenAnswer(inv -> {
            FridgeShoppingHistory h = inv.getArgument(0);
            h.setId(System.nanoTime());
            return 1;
        });

        FridgeShoppingHistoryVO vo = service.confirm(req, 1L);

        assertNotNull(vo.getBatchId());
        assertEquals(2, vo.getItems().size());
        assertEquals("partner@example.com", vo.getPartnerEmail());
        assertTrue(vo.getEmailSent());
        assertEquals(2, captor.getAllValues().size());
        assertEquals(1L, captor.getAllValues().get(0).getUserId());
        assertEquals(vo.getBatchId(), captor.getAllValues().get(0).getBatchId());
        verify(mailClient).sendMessage(any(PurchaseNoticeEmail.class));
    }

    @Test
    void shouldThrowOnConfirmWithEmptyItems() {
        FridgeShoppingConfirmRequest req = new FridgeShoppingConfirmRequest();
        req.setItems(new ArrayList<>());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.confirm(req, 1L));
        assertEquals("采购篮为空", ex.getMessage());
        verify(historyMapper, never()).insert(any(FridgeShoppingHistory.class));
    }

    @Test
    void shouldUseDefaultQuantityWhenZero() {
        FridgeShoppingConfirmRequest req = new FridgeShoppingConfirmRequest();
        FridgeShoppingItem it = item("苹果");
        it.setQuantity(BigDecimal.ZERO);
        req.setItems(List.of(it));

        ArgumentCaptor<FridgeShoppingHistory> captor = ArgumentCaptor.forClass(FridgeShoppingHistory.class);
        when(historyMapper.insert(captor.capture())).thenAnswer(inv -> 1);

        service.confirm(req, 1L);

        assertEquals(BigDecimal.ONE, captor.getValue().getQuantity());
    }

    @Test
    void shouldListHistoryGroupedByBatch() {
        String batchA = java.util.UUID.randomUUID().toString();
        String batchB = java.util.UUID.randomUUID().toString();
        FridgeShoppingHistory r1 = newRow(1L, 1L, batchA, "鸡蛋");
        FridgeShoppingHistory r2 = newRow(2L, 1L, batchA, "西红柿");
        FridgeShoppingHistory r3 = newRow(3L, 1L, batchB, "牛奶");
        when(historyMapper.selectList(any())).thenReturn(List.of(r1, r2, r3));

        List<FridgeShoppingHistoryVO> result = service.listHistory(1L, 50);

        assertEquals(2, result.size());
        assertEquals(batchA, result.get(0).getBatchId());
        assertEquals(2, result.get(0).getItems().size());
        assertEquals(batchB, result.get(1).getBatchId());
    }

    @Test
    void shouldReuseBatchReturnItems() {
        String batch = java.util.UUID.randomUUID().toString();
        FridgeShoppingHistory r1 = newRow(1L, 1L, batch, "鸡蛋");
        FridgeShoppingHistory r2 = newRow(2L, 1L, batch, "西红柿");
        when(historyMapper.selectList(any())).thenReturn(List.of(r1, r2));

        List<FridgeShoppingItem> items = service.reuseBatch(batch, 1L);

        assertEquals(2, items.size());
        assertEquals("鸡蛋", items.get(0).getName());
        assertEquals("西红柿", items.get(1).getName());
    }

    @Test
    void shouldSendEmailToGroupMembers() {
        FridgeShoppingConfirmRequest req = new FridgeShoppingConfirmRequest();
        req.setItems(List.of(item("鸡蛋")));
        req.setGroupId(7L);

        when(historyMapper.insert(any(FridgeShoppingHistory.class))).thenAnswer(inv -> {
            FridgeShoppingHistory h = inv.getArgument(0);
            h.setId(System.nanoTime());
            return 1;
        });
        when(friendClient.getGroupMembers(7L)).thenReturn(Result.ok(List.of(2L, 3L)));
        when(memberEmailClient.getMemberEmail(2L)).thenReturn(Result.ok("a@x.com"));
        when(memberEmailClient.getMemberEmail(3L)).thenReturn(Result.ok("b@x.com"));

        FridgeShoppingHistoryVO vo = service.confirm(req, 1L);

        assertTrue(vo.getEmailSent());
        verify(mailClient, times(2)).sendMessage(any(PurchaseNoticeEmail.class));
    }

    @Test
    void shouldNotFailWhenGroupEmailThrows() {
        FridgeShoppingConfirmRequest req = new FridgeShoppingConfirmRequest();
        req.setItems(List.of(item("鸡蛋")));
        req.setGroupId(7L);

        when(historyMapper.insert(any(FridgeShoppingHistory.class))).thenAnswer(inv -> {
            FridgeShoppingHistory h = inv.getArgument(0);
            h.setId(System.nanoTime());
            return 1;
        });
        when(friendClient.getGroupMembers(anyLong())).thenThrow(new RuntimeException("down"));

        FridgeShoppingHistoryVO vo = service.confirm(req, 1L);

        assertFalse(vo.getEmailSent());
        verify(mailClient, never()).sendMessage(any(PurchaseNoticeEmail.class));
    }

    private FridgeShoppingHistory newRow(Long id, Long userId, String batchId, String name) {
        FridgeShoppingHistory r = new FridgeShoppingHistory();
        r.setId(id);
        r.setUserId(userId);
        r.setBatchId(batchId);
        r.setName(name);
        r.setQuantity(BigDecimal.ONE);
        return r;
    }

    @Test
    void shouldClearBasketItemsWhenBasketItemIdsProvided() {
        FridgeShoppingConfirmRequest req = new FridgeShoppingConfirmRequest();
        req.setItems(List.of(item("鸡蛋"), item("西红柿")));
        req.setBasketItemIds(List.of(101L, 102L));

        when(historyMapper.insert(any(FridgeShoppingHistory.class))).thenAnswer(inv -> {
            FridgeShoppingHistory h = inv.getArgument(0);
            h.setId(System.nanoTime());
            return 1;
        });
        when(itemMapper.delete(any(Wrapper.class))).thenReturn(2);

        FridgeShoppingHistoryVO vo = service.confirm(req, 1L);

        assertNotNull(vo.getBatchId());
        verify(itemMapper).delete(any(Wrapper.class));
    }

    @Test
    void shouldSkipBasketClearWhenBasketItemIdsEmpty() {
        FridgeShoppingConfirmRequest req = new FridgeShoppingConfirmRequest();
        req.setItems(List.of(item("鸡蛋")));

        when(historyMapper.insert(any(FridgeShoppingHistory.class))).thenAnswer(inv -> {
            FridgeShoppingHistory h = inv.getArgument(0);
            h.setId(System.nanoTime());
            return 1;
        });

        service.confirm(req, 1L);

        verify(itemMapper, never()).delete(any(Wrapper.class));
    }

    @Test
    void shouldSkipBasketClearWhenBasketItemIdsNull() {
        FridgeShoppingConfirmRequest req = new FridgeShoppingConfirmRequest();
        req.setItems(List.of(item("鸡蛋")));
        req.setBasketItemIds(null);

        when(historyMapper.insert(any(FridgeShoppingHistory.class))).thenAnswer(inv -> {
            FridgeShoppingHistory h = inv.getArgument(0);
            h.setId(System.nanoTime());
            return 1;
        });

        service.confirm(req, 1L);

        verify(itemMapper, never()).delete(any(Wrapper.class));
    }

    @Test
    void shouldSendPartnerVariantWithItems() {
        FridgeShoppingItem milk = item("牛奶");
        milk.setUnit("瓶");
        FridgeShoppingConfirmRequest req = new FridgeShoppingConfirmRequest();
        req.setItems(List.of(milk));
        req.setPartnerEmail("partner@example.com");

        when(historyMapper.insert(any(FridgeShoppingHistory.class))).thenAnswer(inv -> {
            FridgeShoppingHistory h = inv.getArgument(0);
            h.setId(System.nanoTime());
            return 1;
        });

        ArgumentCaptor<PurchaseNoticeEmail> captor = ArgumentCaptor.forClass(PurchaseNoticeEmail.class);
        service.confirm(req, 1L);

        verify(mailClient).sendMessage(captor.capture());
        PurchaseNoticeEmail sent = captor.getValue();
        assertEquals("partner@example.com", sent.getTo());
        assertEquals(Variant.PARTNER, sent.getVariant());
        assertEquals(1, sent.getItems().size());
        assertEquals("牛奶", sent.getItems().get(0).getName());
        assertEquals("瓶", sent.getItems().get(0).getUnit());
        assertTrue(sent.getDefaultSubject().contains("采购清单"));
        assertTrue(sent.toVariables().get("itemsTable").toString().contains("牛奶"));
    }
}
