package com.homecentral.fridge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.homecentral.fridge.api.dto.FridgeItemRequest;
import com.homecentral.fridge.api.dto.FridgeMigrateRequest;
import com.homecentral.fridge.api.enums.FridgeItemStatus;
import com.homecentral.fridge.api.enums.FridgeItemSource;
import com.homecentral.fridge.api.enums.FridgeLayout;
import com.homecentral.fridge.api.enums.FridgeZone;
import com.homecentral.fridge.api.vo.FridgeExpiringVO;
import com.homecentral.fridge.api.vo.FridgeItemVO;
import com.homecentral.fridge.api.vo.FridgeMigrateResultVO;
import com.homecentral.fridge.entity.FridgeCategory;
import com.homecentral.fridge.entity.FridgeItem;
import com.homecentral.fridge.mapper.FridgeCategoryMapper;
import com.homecentral.fridge.mapper.FridgeItemMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FridgeItemServiceImplTest {

    @Mock
    private FridgeItemMapper itemMapper;

    @Mock
    private FridgeCategoryMapper categoryMapper;

    @InjectMocks
    private FridgeItemServiceImpl service;

    private FridgeItemRequest newRequest() {
        FridgeItemRequest req = new FridgeItemRequest();
        req.setName("纯牛奶");
        req.setZone(FridgeZone.REFRIGERATED);
        req.setSubZone("冷藏-门上");
        req.setQuantity(BigDecimal.ONE);
        req.setUnit("盒");
        req.setPurchaseDate(LocalDate.now());
        req.setExpiryDate(LocalDate.now().plusDays(7));
        return req;
    }

    private FridgeItem seed(Long id, FridgeItemStatus status, LocalDate expiry, Long createdBy) {
        FridgeItem i = new FridgeItem();
        i.setId(id);
        i.setName("鸡蛋");
        i.setZone(FridgeZone.REFRIGERATED);
        i.setStatus(status);
        i.setExpiryDate(expiry);
        i.setCreatedBy(createdBy);
        i.setSource(FridgeItemSource.MANUAL);
        return i;
    }

    @Test
    void shouldCreateItemWithDefaults() {
        FridgeItemRequest req = newRequest();
        ArgumentCaptor<FridgeItem> captor = ArgumentCaptor.forClass(FridgeItem.class);
        when(itemMapper.insert(captor.capture())).thenAnswer(inv -> {
            FridgeItem i = inv.getArgument(0);
            i.setId(1L);
            return 1;
        });

        FridgeItemVO vo = service.create(req, 1L);

        assertEquals(1L, vo.getId());
        assertEquals("纯牛奶", vo.getName());
        assertEquals(FridgeZone.REFRIGERATED, vo.getZone());
        assertEquals(FridgeItemStatus.ACTIVE, vo.getStatus());
        assertEquals(FridgeItemSource.MANUAL, vo.getSource());
    }

    @Test
    void shouldBatchCreateAllItems() {
        FridgeItemRequest a = newRequest();
        FridgeItemRequest b = newRequest();
        b.setName("酸奶");
        b.setSubZone("冷藏-中层");

        ArgumentCaptor<FridgeItem> captor = ArgumentCaptor.forClass(FridgeItem.class);
        when(itemMapper.insert(captor.capture())).thenAnswer(inv -> {
            FridgeItem i = inv.getArgument(0);
            i.setId((long) (Math.random() * 1000));
            return 1;
        });

        List<FridgeItemVO> result = service.batchCreate(List.of(a, b), 1L);

        assertEquals(2, result.size());
        verify(itemMapper, org.mockito.Mockito.times(2)).insert(captor.capture());
    }

    @Test
    void shouldReturnEmptyListOnBatchCreateWithNullInput() {
        assertTrue(service.batchCreate(null, 1L).isEmpty());
        assertTrue(service.batchCreate(new ArrayList<>(), 1L).isEmpty());
    }

    @Test
    void shouldUpdateItem() {
        FridgeItem existing = seed(1L, FridgeItemStatus.ACTIVE, LocalDate.now().plusDays(3), 1L);
        when(itemMapper.selectById(1L)).thenReturn(existing);

        FridgeItemRequest req = newRequest();
        req.setName("低脂牛奶");
        req.setQuantity(BigDecimal.valueOf(2));

        FridgeItemVO vo = service.update(1L, req, 1L);

        assertEquals("低脂牛奶", vo.getName());
        assertEquals(BigDecimal.valueOf(2), vo.getQuantity());
    }

    @Test
    void shouldThrowWhenUpdateConsumedItem() {
        FridgeItem consumed = seed(1L, FridgeItemStatus.CONSUMED, null, 1L);
        when(itemMapper.selectById(1L)).thenReturn(consumed);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.update(1L, newRequest(), 1L));
        assertEquals("已消耗的食材不可修改", ex.getMessage());
    }

    @Test
    void shouldConsumeItem() {
        FridgeItem existing = seed(1L, FridgeItemStatus.ACTIVE, null, 1L);
        when(itemMapper.selectById(1L)).thenReturn(existing);

        FridgeItemVO vo = service.consume(1L, 1L);

        assertEquals(FridgeItemStatus.CONSUMED, vo.getStatus());
        assertNotNull(vo.getConsumedAt());
    }

    @Test
    void shouldConsumeOneWhenQuantityGreaterThanOne() {
        FridgeItem existing = seed(1L, FridgeItemStatus.ACTIVE, null, 1L);
        existing.setQuantity(BigDecimal.valueOf(3));
        when(itemMapper.selectById(1L)).thenReturn(existing);

        FridgeItemVO vo = service.consumeOne(1L, 1L);

        assertEquals(FridgeItemStatus.ACTIVE, vo.getStatus());
        assertEquals(BigDecimal.valueOf(2), vo.getQuantity());
        assertNull(vo.getConsumedAt());
    }

    @Test
    void shouldConsumeOneAndMarkConsumedWhenQuantityEqualsOne() {
        FridgeItem existing = seed(1L, FridgeItemStatus.ACTIVE, null, 1L);
        existing.setQuantity(BigDecimal.ONE);
        when(itemMapper.selectById(1L)).thenReturn(existing);

        FridgeItemVO vo = service.consumeOne(1L, 1L);

        assertEquals(FridgeItemStatus.CONSUMED, vo.getStatus());
        assertNotNull(vo.getConsumedAt());
    }

    @Test
    void shouldThrowWhenConsumeOneOnNonActiveItem() {
        FridgeItem consumed = seed(1L, FridgeItemStatus.CONSUMED, null, 1L);
        when(itemMapper.selectById(1L)).thenReturn(consumed);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.consumeOne(1L, 1L));
        assertEquals("仅在库的食材可标记消耗", ex.getMessage());
    }

    @Test
    void shouldDeleteActiveItem() {
        FridgeItem existing = seed(1L, FridgeItemStatus.ACTIVE, null, 1L);
        when(itemMapper.selectById(1L)).thenReturn(existing);

        service.delete(1L, 1L);

        verify(itemMapper).deleteById(1L);
    }

    @Test
    void shouldThrowWhenDeleteNonActive() {
        FridgeItem consumed = seed(1L, FridgeItemStatus.CONSUMED, null, 1L);
        when(itemMapper.selectById(1L)).thenReturn(consumed);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.delete(1L, 1L));
        assertEquals("仅在库的食材可删除", ex.getMessage());
    }

    @Test
    void shouldExposeCategoryFieldsOnList() {
        FridgeItem item = seed(1L, FridgeItemStatus.ACTIVE, LocalDate.now().plusDays(5), 1L);
        item.setCategoryId(2L);

        FridgeCategory cat = new FridgeCategory();
        cat.setId(2L);
        cat.setName("乳制品");
        cat.setIcon("🥛");
        cat.setColor("#F59E0B");

        when(itemMapper.selectList(any())).thenReturn(List.of(item));
        when(categoryMapper.selectBatchIds(org.mockito.ArgumentMatchers.anyCollection())).thenReturn(List.of(cat));

        List<FridgeItemVO> result = service.list(1L, null, null, null, false);

        assertEquals(1, result.size());
        FridgeItemVO vo = result.get(0);
        assertEquals("乳制品", vo.getCategoryName());
        assertEquals("🥛", vo.getCategoryIcon());
        assertEquals("#F59E0B", vo.getCategoryColor());
        assertNotNull(vo.getDaysToExpiry());
    }

    @Test
    void shouldIncludePendingOnListWhenRequested() {
        FridgeItem active = seed(1L, FridgeItemStatus.ACTIVE, LocalDate.now().plusDays(2), 1L);
        when(itemMapper.selectList(any())).thenReturn(List.of(active));
        List<FridgeItemVO> result = service.list(1L, null, null, null, true);
        assertEquals(1, result.size());
    }

    @Test
    void shouldComputeExpiringStats() {
        FridgeItem expired = seed(1L, FridgeItemStatus.ACTIVE, LocalDate.now().minusDays(1), 1L);
        FridgeItem expiring = seed(2L, FridgeItemStatus.ACTIVE, LocalDate.now().plusDays(2), 1L);
        FridgeItem fresh = seed(3L, FridgeItemStatus.ACTIVE, LocalDate.now().plusDays(30), 1L);
        when(itemMapper.selectList(any())).thenReturn(List.of(expired, expiring, fresh));

        FridgeExpiringVO stats = service.expiringStats(1L, 3);

        assertEquals(1, stats.getExpiredCount());
        assertEquals(1, stats.getExpiringCount());
        assertEquals(3, stats.getThresholdDays());
    }

    @Test
    void shouldMoveItemToNewZoneAndSubZone() {
        FridgeItem existing = seed(1L, FridgeItemStatus.ACTIVE, null, 1L);
        when(itemMapper.selectById(1L)).thenReturn(existing);

        com.homecentral.fridge.api.dto.FridgeItemMoveRequest req = new com.homecentral.fridge.api.dto.FridgeItemMoveRequest();
        req.setZone(FridgeZone.FROZEN);
        req.setSubZone("FROZEN-L1");

        FridgeItemVO vo = service.move(1L, req, 1L);

        assertEquals(FridgeZone.FROZEN, vo.getZone());
        assertEquals("FROZEN-L1", vo.getSubZone());
        assertEquals(FridgeItemStatus.ACTIVE, vo.getStatus());
    }

    @Test
    void shouldMovePendingItemToActiveOnPlacement() {
        FridgeItem existing = seed(1L, FridgeItemStatus.PENDING, null, 1L);
        when(itemMapper.selectById(1L)).thenReturn(existing);

        com.homecentral.fridge.api.dto.FridgeItemMoveRequest req = new com.homecentral.fridge.api.dto.FridgeItemMoveRequest();
        req.setZone(FridgeZone.REFRIGERATED);
        req.setSubZone("REFRIGERATED-L1");

        FridgeItemVO vo = service.move(1L, req, 1L);

        assertEquals(FridgeItemStatus.ACTIVE, vo.getStatus());
        assertEquals("REFRIGERATED-L1", vo.getSubZone());
    }

    @Test
    void shouldMoveItemBackToBasket() {
        FridgeItem existing = seed(1L, FridgeItemStatus.ACTIVE, null, 1L);
        when(itemMapper.selectById(1L)).thenReturn(existing);

        com.homecentral.fridge.api.dto.FridgeItemMoveRequest req = new com.homecentral.fridge.api.dto.FridgeItemMoveRequest();
        // zone + subZone 都为 null -> 退回采购篮

        FridgeItemVO vo = service.move(1L, req, 1L);

        assertEquals(FridgeItemStatus.PENDING, vo.getStatus());
        assertNull(vo.getZone());
        assertNull(vo.getSubZone());
    }

    @Test
    void shouldThrowWhenMoveConsumedItem() {
        FridgeItem consumed = seed(1L, FridgeItemStatus.CONSUMED, null, 1L);
        when(itemMapper.selectById(1L)).thenReturn(consumed);

        com.homecentral.fridge.api.dto.FridgeItemMoveRequest req = new com.homecentral.fridge.api.dto.FridgeItemMoveRequest();
        req.setZone(FridgeZone.FROZEN);
        req.setSubZone("FROZEN-L1");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.move(1L, req, 1L));
        assertEquals("已消耗的食材不可移动", ex.getMessage());
    }

    @Test
    void shouldThrowWhenMoveOthersItem() {
        FridgeItem others = seed(1L, FridgeItemStatus.ACTIVE, null, 2L);
        when(itemMapper.selectById(1L)).thenReturn(others);

        com.homecentral.fridge.api.dto.FridgeItemMoveRequest req = new com.homecentral.fridge.api.dto.FridgeItemMoveRequest();
        req.setZone(FridgeZone.FROZEN);
        req.setSubZone("FROZEN-L1");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.move(1L, req, 1L));
        assertEquals("只能移动自己添加的食材", ex.getMessage());
    }

    @Test
    void shouldQuickCreatePendingItem() {
        com.homecentral.fridge.api.dto.FridgeQuickCreateRequest req = new com.homecentral.fridge.api.dto.FridgeQuickCreateRequest();
        req.setName("鸡蛋");
        // zone + subZone 都为 null -> PENDING
        when(itemMapper.insert(any(FridgeItem.class))).thenAnswer(inv -> {
            FridgeItem i = inv.getArgument(0);
            i.setId(99L);
            return 1;
        });

        FridgeItemVO vo = service.quickCreate(req, 1L);

        assertEquals(99L, vo.getId());
        assertEquals("鸡蛋", vo.getName());
        assertEquals(FridgeItemStatus.PENDING, vo.getStatus());
        assertNull(vo.getZone());
    }

    @Test
    void shouldQuickCreateActiveItemWhenZoneProvided() {
        com.homecentral.fridge.api.dto.FridgeQuickCreateRequest req = new com.homecentral.fridge.api.dto.FridgeQuickCreateRequest();
        req.setName("鸡蛋");
        req.setZone(FridgeZone.REFRIGERATED);
        req.setSubZone("REFRIGERATED-L1");
        when(itemMapper.insert(any(FridgeItem.class))).thenAnswer(inv -> {
            FridgeItem i = inv.getArgument(0);
            i.setId(100L);
            return 1;
        });

        FridgeItemVO vo = service.quickCreate(req, 1L);

        assertEquals(100L, vo.getId());
        assertEquals(FridgeItemStatus.ACTIVE, vo.getStatus());
        assertEquals(FridgeZone.REFRIGERATED, vo.getZone());
        assertEquals("REFRIGERATED-L1", vo.getSubZone());
    }

    // ---- migrateOnLayoutChange ----

    private FridgeMigrateRequest migrateReq(FridgeLayout layout, int f, int fr, int c, int d) {
        FridgeMigrateRequest r = new FridgeMigrateRequest();
        r.setLayout(layout);
        r.setFridgeLayers(f);
        r.setFreezerLayers(fr);
        r.setChillerLayers(c);
        r.setDoorShelfCount(d);
        return r;
    }

    private FridgeItem mkItem(Long id, FridgeZone zone, String sub, FridgeItemStatus status, Long createdBy) {
        FridgeItem i = new FridgeItem();
        i.setId(id);
        i.setName("item-" + id);
        i.setZone(zone);
        i.setSubZone(sub);
        i.setStatus(status);
        i.setCreatedBy(createdBy);
        return i;
    }

    @Test
    void migrate_noUserId_returnsZeros() {
        FridgeMigrateResultVO vo = service.migrateOnLayoutChange(
                migrateReq(FridgeLayout.CLASSIC, 3, 2, 0, 0), null);
        assertEquals(0, vo.getScannedCount());
        assertEquals(0, vo.getMigratedCount());
    }

    @Test
    void migrate_emptyDb_returnsZeros() {
        when(itemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        FridgeMigrateResultVO vo = service.migrateOnLayoutChange(
                migrateReq(FridgeLayout.CLASSIC, 3, 2, 0, 0), 1L);
        assertEquals(0, vo.getScannedCount());
        assertEquals(0, vo.getMigratedCount());
    }

    @Test
    void migrate_chillerDisappears_whenLayoutNotThreeDoor() {
        FridgeItem chillerItem = mkItem(1L, FridgeZone.REFRIGERATED, "CHILLER-L1", FridgeItemStatus.ACTIVE, 1L);
        when(itemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(chillerItem));
        when(itemMapper.update(any(), any(UpdateWrapper.class))).thenReturn(1);

        FridgeMigrateResultVO vo = service.migrateOnLayoutChange(
                migrateReq(FridgeLayout.CLASSIC, 3, 2, 0, 0), 1L);

        assertEquals(1, vo.getScannedCount());
        assertEquals(1, vo.getMigratedCount());
    }

    @Test
    void migrate_doorLeft_keptInSideBySide_migratedInClassic() {
        FridgeItem leftDoor = mkItem(2L, FridgeZone.FROZEN, "DOOR-LEFT-L1", FridgeItemStatus.ACTIVE, 1L);

        // SIDE_BY_SIDE → 保留
        when(itemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(leftDoor));
        FridgeMigrateResultVO kept = service.migrateOnLayoutChange(
                migrateReq(FridgeLayout.SIDE_BY_SIDE, 3, 3, 0, 3), 1L);
        assertEquals(0, kept.getMigratedCount());

        // CLASSIC → 迁出（左门只有 SIDE_BY_SIDE 有）
        when(itemMapper.update(any(), any(UpdateWrapper.class))).thenReturn(1);
        FridgeMigrateResultVO moved = service.migrateOnLayoutChange(
                migrateReq(FridgeLayout.CLASSIC, 3, 2, 0, 0), 1L);
        assertEquals(1, moved.getMigratedCount());
    }

    @Test
    void migrate_doorRight_keptInThreeDoor_migratedInBottomFreezer() {
        FridgeItem rightDoor = mkItem(3L, FridgeZone.REFRIGERATED, "DOOR-RIGHT-L1", FridgeItemStatus.ACTIVE, 1L);
        when(itemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(rightDoor));

        // THREE_DOOR → 保留
        FridgeMigrateResultVO kept = service.migrateOnLayoutChange(
                migrateReq(FridgeLayout.THREE_DOOR, 3, 2, 1, 3), 1L);
        assertEquals(0, kept.getMigratedCount());

        // BOTTOM_FREEZER → 迁出
        when(itemMapper.update(any(), any(UpdateWrapper.class))).thenReturn(1);
        FridgeMigrateResultVO moved = service.migrateOnLayoutChange(
                migrateReq(FridgeLayout.BOTTOM_FREEZER, 3, 2, 0, 0), 1L);
        assertEquals(1, moved.getMigratedCount());
    }

    @Test
    void migrate_layerOverflow_migrated() {
        FridgeItem tooDeep = mkItem(4L, FridgeZone.REFRIGERATED, "REFRIGERATED-L5", FridgeItemStatus.ACTIVE, 1L);
        when(itemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(tooDeep));
        when(itemMapper.update(any(), any(UpdateWrapper.class))).thenReturn(1);

        // 新冷藏只有 3 层，原 5 层应被迁出
        FridgeMigrateResultVO vo = service.migrateOnLayoutChange(
                migrateReq(FridgeLayout.CLASSIC, 3, 2, 0, 0), 1L);
        assertEquals(1, vo.getMigratedCount());
    }

    @Test
    void migrate_doorShelfOverflow_migrated() {
        FridgeItem tooDeep = mkItem(5L, FridgeZone.FROZEN, "DOOR-LEFT-L5", FridgeItemStatus.ACTIVE, 1L);
        when(itemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(tooDeep));
        when(itemMapper.update(any(), any(UpdateWrapper.class))).thenReturn(1);

        // 门搁板只 2 个
        FridgeMigrateResultVO vo = service.migrateOnLayoutChange(
                migrateReq(FridgeLayout.SIDE_BY_SIDE, 3, 3, 0, 2), 1L);
        assertEquals(1, vo.getMigratedCount());
    }

    @Test
    void migrate_chineseFreeTextSubZone_keptInAnyLayout() {
        // 中文 subZone（"中层"）不在严格编码里 → 不应被迁出
        FridgeItem cn = mkItem(6L, FridgeZone.REFRIGERATED, "中层", FridgeItemStatus.ACTIVE, 1L);
        when(itemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(cn));

        FridgeMigrateResultVO vo = service.migrateOnLayoutChange(
                migrateReq(FridgeLayout.CLASSIC, 3, 2, 0, 0), 1L);
        assertEquals(0, vo.getMigratedCount());
    }

    @Test
    void migrate_mixedBatch_onlyIncompatibleMigrated() {
        // 5 条：2 条兼容 + 3 条不兼容（chiller 消失 / 5 层超界 / 5 门超界）
        FridgeItem ok1 = mkItem(10L, FridgeZone.REFRIGERATED, "REFRIGERATED-L1", FridgeItemStatus.ACTIVE, 1L);
        FridgeItem ok2 = mkItem(11L, FridgeZone.FROZEN, "FROZEN-L2", FridgeItemStatus.ACTIVE, 1L);
        FridgeItem chiller = mkItem(12L, FridgeZone.REFRIGERATED, "CHILLER-L1", FridgeItemStatus.ACTIVE, 1L);
        FridgeItem deep = mkItem(13L, FridgeZone.REFRIGERATED, "REFRIGERATED-L4", FridgeItemStatus.ACTIVE, 1L);
        FridgeItem door = mkItem(14L, FridgeZone.FROZEN, "DOOR-LEFT-L4", FridgeItemStatus.ACTIVE, 1L);
        when(itemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(ok1, ok2, chiller, deep, door));
        when(itemMapper.update(any(), any(UpdateWrapper.class))).thenReturn(3);

        // 切到 CLASSIC: chiller 走 (12), REFRIGERATED-L4 越界 (13), DOOR-LEFT-L4 在 CLASSIC 不存在 (14) → 共 3 条
        FridgeMigrateResultVO vo = service.migrateOnLayoutChange(
                migrateReq(FridgeLayout.CLASSIC, 3, 2, 0, 0), 1L);
        assertEquals(5, vo.getScannedCount());
        assertEquals(3, vo.getMigratedCount());
    }
}
