package com.homecentral.life.service.impl;

import com.homecentral.life.api.dto.AnniversaryCreateRequest;
import com.homecentral.life.api.dto.ReminderRuleCreateRequest;
import com.homecentral.life.api.dto.ShoppingMemoCreateRequest;
import com.homecentral.life.api.vo.AnniversaryVO;
import com.homecentral.life.api.vo.ReminderRuleVO;
import com.homecentral.life.api.vo.ShoppingMemoVO;
import com.homecentral.life.entity.Anniversary;
import com.homecentral.life.entity.ReminderRule;
import com.homecentral.life.entity.ShoppingMemo;
import com.homecentral.life.mapper.AnniversaryMapper;
import com.homecentral.life.mapper.ReminderRuleMapper;
import com.homecentral.life.mapper.ShoppingMemoMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LifeServiceImplTest {

    @Mock
    private ShoppingMemoMapper shoppingMemoMapper;

    @Mock
    private AnniversaryMapper anniversaryMapper;

    @Mock
    private ReminderRuleMapper reminderRuleMapper;

    @InjectMocks
    private LifeServiceImpl lifeService;

    @Test
    void shouldCreateShoppingMemo() {
        ShoppingMemoCreateRequest request = new ShoppingMemoCreateRequest();
        request.setItemName("牛奶");
        request.setNote("买2箱");

        ArgumentCaptor<ShoppingMemo> captor = ArgumentCaptor.forClass(ShoppingMemo.class);
        when(shoppingMemoMapper.insert(captor.capture())).thenReturn(1);

        ShoppingMemoVO vo = lifeService.createShoppingMemo(request, 1L);

        assertNotNull(vo);
        assertEquals("牛奶", vo.getItemName());
        assertFalse(vo.isPurchased());
        ShoppingMemo captured = captor.getValue();
        assertFalse(captured.getPurchased());
        assertEquals(1L, captured.getCreatedBy());
    }

    @Test
    void shouldListShoppingMemos() {
        ShoppingMemo memo = new ShoppingMemo();
        memo.setId(1L);
        memo.setItemName("面包");
        memo.setPurchased(false);

        when(shoppingMemoMapper.selectList(any())).thenReturn(List.of(memo));

        List<ShoppingMemoVO> list = lifeService.listShoppingMemos(1L);

        assertEquals(1, list.size());
        assertEquals("面包", list.getFirst().getItemName());
    }

    @Test
    void shouldReturnEmptyShoppingMemoList() {
        when(shoppingMemoMapper.selectList(any())).thenReturn(List.of());

        List<ShoppingMemoVO> list = lifeService.listShoppingMemos(1L);

        assertTrue(list.isEmpty());
    }

    @Test
    void shouldTogglePurchased() {
        ShoppingMemo memo = new ShoppingMemo();
        memo.setId(1L);
        memo.setPurchased(false);

        when(shoppingMemoMapper.selectById(1L)).thenReturn(memo);

        ShoppingMemoVO vo = lifeService.togglePurchased(1L, 1L);

        assertTrue(vo.isPurchased());
        verify(shoppingMemoMapper).updateById(memo);
    }

    @Test
    void shouldThrowWhenToggleNonExistentMemo() {
        when(shoppingMemoMapper.selectById(999L)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> lifeService.togglePurchased(999L, 1L));
        assertEquals("购物项不存在", ex.getMessage());
    }

    @Test
    void shouldCreateAnniversary() {
        AnniversaryCreateRequest request = new AnniversaryCreateRequest();
        request.setTitle("结婚纪念日");
        request.setEventDate(LocalDate.of(2025, 6, 1));
        request.setRemindBeforeDays(3);

        ArgumentCaptor<Anniversary> captor = ArgumentCaptor.forClass(Anniversary.class);
        when(anniversaryMapper.insert(captor.capture())).thenReturn(1);

        AnniversaryVO vo = lifeService.createAnniversary(request, 1L);

        assertNotNull(vo);
        assertEquals("结婚纪念日", vo.getTitle());
        assertEquals(LocalDate.of(2025, 6, 1), vo.getEventDate());
        assertEquals(3, vo.getRemindBeforeDays());
    }

    @Test
    void shouldCreateAnniversaryWithDefaultRemindDays() {
        AnniversaryCreateRequest request = new AnniversaryCreateRequest();
        request.setTitle("生日");
        request.setEventDate(LocalDate.of(2025, 12, 25));

        ArgumentCaptor<Anniversary> captor = ArgumentCaptor.forClass(Anniversary.class);
        when(anniversaryMapper.insert(captor.capture())).thenReturn(1);

        AnniversaryVO vo = lifeService.createAnniversary(request, 1L);

        assertEquals(0, vo.getRemindBeforeDays());
    }

    @Test
    void shouldListAnniversaries() {
        Anniversary ann = new Anniversary();
        ann.setId(1L);
        ann.setTitle("劳动节");

        when(anniversaryMapper.selectList(any())).thenReturn(List.of(ann));

        List<AnniversaryVO> list = lifeService.listAnniversaries(1L);

        assertEquals(1, list.size());
        assertEquals("劳动节", list.getFirst().getTitle());
    }

    @Test
    void shouldCreateReminderRule() {
        ReminderRuleCreateRequest request = new ReminderRuleCreateRequest();
        request.setTitle("浇水提醒");
        request.setContent("记得给植物浇水");
        request.setCronExpression("0 0 8 * * ?");
        request.setEnabled(true);

        ArgumentCaptor<ReminderRule> captor = ArgumentCaptor.forClass(ReminderRule.class);
        when(reminderRuleMapper.insert(captor.capture())).thenReturn(1);

        ReminderRuleVO vo = lifeService.createReminderRule(request, 1L);

        assertNotNull(vo);
        assertEquals("浇水提醒", vo.getTitle());
        assertTrue(vo.getEnabled());
    }

    @Test
    void shouldCreateReminderRuleWithDefaultEnabled() {
        ReminderRuleCreateRequest request = new ReminderRuleCreateRequest();
        request.setTitle("默认开启");
        request.setCronExpression("0 0 9 * * ?");

        ArgumentCaptor<ReminderRule> captor = ArgumentCaptor.forClass(ReminderRule.class);
        when(reminderRuleMapper.insert(captor.capture())).thenReturn(1);

        ReminderRuleVO vo = lifeService.createReminderRule(request, 1L);

        assertTrue(vo.getEnabled());
    }

    @Test
    void shouldListReminderRules() {
        ReminderRule rule = new ReminderRule();
        rule.setId(1L);
        rule.setTitle("运动提醒");

        when(reminderRuleMapper.selectList(any())).thenReturn(List.of(rule));

        List<ReminderRuleVO> list = lifeService.listReminderRules(1L);

        assertEquals(1, list.size());
        assertEquals("运动提醒", list.getFirst().getTitle());
    }

    @Test
    void shouldToggleEnabled() {
        ReminderRule rule = new ReminderRule();
        rule.setId(1L);
        rule.setEnabled(false);

        when(reminderRuleMapper.selectById(1L)).thenReturn(rule);

        ReminderRuleVO vo = lifeService.toggleEnabled(1L, 1L);

        assertTrue(vo.getEnabled());
        verify(reminderRuleMapper).updateById(rule);
    }

    @Test
    void shouldThrowWhenToggleNonExistentRule() {
        when(reminderRuleMapper.selectById(999L)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> lifeService.toggleEnabled(999L, 1L));
        assertEquals("提醒规则不存在", ex.getMessage());
    }
}
