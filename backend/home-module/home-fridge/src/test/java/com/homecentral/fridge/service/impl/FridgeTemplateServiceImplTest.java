package com.homecentral.fridge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homecentral.fridge.api.dto.FridgeTemplateRequest;
import com.homecentral.fridge.api.enums.FridgeLayout;
import com.homecentral.fridge.api.vo.FridgeTemplateVO;
import com.homecentral.fridge.entity.FridgeTemplate;
import com.homecentral.fridge.mapper.FridgeTemplateMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FridgeTemplateServiceImplTest {

    @Mock
    private FridgeTemplateMapper templateMapper;

    @InjectMocks
    private FridgeTemplateServiceImpl service;

    private FridgeTemplate systemTemplate(Long id) {
        FridgeTemplate t = new FridgeTemplate();
        t.setId(id);
        t.setName("经典单开");
        t.setLayout(FridgeLayout.CLASSIC);
        t.setFridgeLayers(3);
        t.setFreezerLayers(2);
        t.setChillerLayers(0);
        t.setDoorShelfCount(3);
        t.setIsDefault(false);
        t.setIsSystem(true);
        return t;
    }

    private FridgeTemplate userTemplate(Long id, Long owner, boolean isDefault) {
        FridgeTemplate t = new FridgeTemplate();
        t.setId(id);
        t.setOwnerId(owner);
        t.setName("我家冰箱");
        t.setLayout(FridgeLayout.SIDE_BY_SIDE);
        t.setFridgeLayers(3);
        t.setFreezerLayers(3);
        t.setChillerLayers(0);
        t.setDoorShelfCount(4);
        t.setIsDefault(isDefault);
        t.setIsSystem(false);
        return t;
    }

    private FridgeTemplateRequest newRequest() {
        FridgeTemplateRequest req = new FridgeTemplateRequest();
        req.setName("我家冰箱");
        req.setLayout(FridgeLayout.SIDE_BY_SIDE);
        req.setFridgeLayers(3);
        req.setFreezerLayers(3);
        req.setDoorShelfCount(4);
        return req;
    }

    @Test
    void shouldReturnUserDefaultWhenExists() {
        FridgeTemplate def = userTemplate(5L, 1L, true);
        when(templateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(def);

        FridgeTemplateVO vo = service.getDefault(1L);

        assertEquals(5L, vo.getId());
        assertTrue(vo.isDefault());
    }

    @Test
    void shouldFallbackToFirstSystemTemplate() {
        when(templateMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(null)                                  // user default query
                .thenReturn(systemTemplate(1L));                   // fallback system query

        FridgeTemplateVO vo = service.getDefault(1L);

        assertEquals(1L, vo.getId());
        assertTrue(vo.isSystem());
        assertFalse(vo.isDefault());
    }

    @Test
    void shouldThrowWhenNoTemplateAtAll() {
        when(templateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.getDefault(1L));
        assertTrue(ex.getMessage().contains("未找到任何冰箱模板"));
    }

    @Test
    void shouldCreateUserTemplate() {
        when(templateMapper.insert(any(FridgeTemplate.class))).thenAnswer(inv -> {
            FridgeTemplate t = inv.getArgument(0);
            t.setId(20L);
            return 1;
        });

        FridgeTemplateVO vo = service.create(newRequest(), 1L);

        assertEquals(20L, vo.getId());
        assertEquals(1L, vo.getOwnerId());
        assertFalse(vo.isDefault());
        assertFalse(vo.isSystem());
    }

    @Test
    void shouldRejectBlankName() {
        FridgeTemplateRequest req = newRequest();
        req.setName("  ");
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.create(req, 1L));
        assertEquals("模板名称不能为空", ex.getMessage());
        verify(templateMapper, never()).insert(any(FridgeTemplate.class));
    }

    @Test
    void shouldUpdateUserTemplate() {
        FridgeTemplate existing = userTemplate(20L, 1L, false);
        when(templateMapper.selectById(20L)).thenReturn(existing);

        FridgeTemplateRequest req = newRequest();
        req.setName("我家冰箱V2");
        req.setLayout(FridgeLayout.BOTTOM_FREEZER);

        FridgeTemplateVO vo = service.update(20L, req, 1L);

        assertEquals("我家冰箱V2", vo.getName());
        assertEquals(FridgeLayout.BOTTOM_FREEZER, vo.getLayout());
    }

    @Test
    void shouldThrowWhenUpdateSystemTemplate() {
        FridgeTemplate sys = systemTemplate(1L);
        when(templateMapper.selectById(1L)).thenReturn(sys);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.update(1L, newRequest(), 1L));
        assertEquals("系统预置模板不可修改", ex.getMessage());
    }

    @Test
    void shouldThrowWhenUpdateOthersTemplate() {
        FridgeTemplate others = userTemplate(20L, 99L, false);
        when(templateMapper.selectById(20L)).thenReturn(others);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.update(20L, newRequest(), 1L));
        assertEquals("只能修改自己创建的模板", ex.getMessage());
    }

    @Test
    void shouldDeleteNonDefaultUserTemplate() {
        FridgeTemplate t = userTemplate(20L, 1L, false);
        when(templateMapper.selectById(20L)).thenReturn(t);

        service.delete(20L, 1L);

        verify(templateMapper).deleteById(20L);
    }

    @Test
    void shouldThrowWhenDeleteDefaultTemplate() {
        FridgeTemplate def = userTemplate(20L, 1L, true);
        when(templateMapper.selectById(20L)).thenReturn(def);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.delete(20L, 1L));
        assertEquals("当前激活的默认模板不可删除，请先激活其他模板", ex.getMessage());
    }

    @Test
    void shouldThrowWhenDeleteSystemTemplate() {
        FridgeTemplate sys = systemTemplate(1L);
        when(templateMapper.selectById(1L)).thenReturn(sys);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.delete(1L, 1L));
        assertEquals("系统预置模板不可删除", ex.getMessage());
    }

    @Test
    void shouldActivateUserTemplateAndClearOthers() {
        FridgeTemplate target = userTemplate(20L, 1L, false);
        FridgeTemplate oldDefault = userTemplate(15L, 1L, true);
        when(templateMapper.selectById(20L)).thenReturn(target);
        when(templateMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(target, oldDefault));
        when(templateMapper.updateById(any(FridgeTemplate.class))).thenAnswer(inv -> 1);

        FridgeTemplateVO vo = service.activate(20L, 1L);

        assertTrue(vo.isDefault());
        ArgumentCaptor<FridgeTemplate> captor = ArgumentCaptor.forClass(FridgeTemplate.class);
        verify(templateMapper, org.mockito.Mockito.atLeast(2)).updateById(captor.capture());
        // 至少有一个 update 是把旧的 default 置 false
        boolean oldCleared = captor.getAllValues().stream()
                .anyMatch(t -> t.getId().equals(15L) && Boolean.FALSE.equals(t.getIsDefault()));
        assertTrue(oldCleared);
        // target 自身被置为 default
        boolean newSet = captor.getAllValues().stream()
                .anyMatch(t -> t.getId().equals(20L) && Boolean.TRUE.equals(t.getIsDefault()));
        assertTrue(newSet);
    }

    @Test
    void shouldCloneSystemTemplateOnActivate() {
        FridgeTemplate sys = systemTemplate(1L);
        FridgeTemplate reloaded = systemTemplate(1L);
        reloaded.setId(50L);
        reloaded.setOwnerId(1L);
        reloaded.setName("经典单开（我的副本）");
        reloaded.setIsDefault(false);
        reloaded.setIsSystem(false);
        when(templateMapper.selectById(1L)).thenReturn(sys);
        // find-or-clone: 先按 name 查 → null，再按 id 回读
        when(templateMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(null)                                  // 按 name 查复用
                .thenReturn(reloaded);                            // fallback（不会调用）
        when(templateMapper.insert(any(FridgeTemplate.class))).thenAnswer(inv -> {
            FridgeTemplate t = inv.getArgument(0);
            t.setId(50L);
            return 1;
        });
        when(templateMapper.selectById(50L)).thenReturn(reloaded);
        when(templateMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(templateMapper.updateById(any(FridgeTemplate.class))).thenAnswer(inv -> 1);

        FridgeTemplateVO vo = service.activate(1L, 1L);

        assertEquals(50L, vo.getId());
        assertEquals(1L, vo.getOwnerId());
        assertTrue(vo.isDefault());
        assertFalse(vo.isSystem());
        // 验证 clone 的命名带"我的副本" + ownerId 设置正确
        ArgumentCaptor<FridgeTemplate> captor = ArgumentCaptor.forClass(FridgeTemplate.class);
        verify(templateMapper).insert(captor.capture());
        FridgeTemplate cloned = captor.getValue();
        assertEquals(1L, cloned.getOwnerId());
        assertNotNull(cloned.getName());
        assertTrue(cloned.getName().contains("我的副本"));
        // 验证 insert 后立刻 selectById 回读了 ownerId
        verify(templateMapper).selectById(50L);
    }

    @Test
    void shouldReuseExistingCloneOnActivate() {
        FridgeTemplate sys = systemTemplate(1L);
        FridgeTemplate existingClone = userTemplate(99L, 1L, false);
        existingClone.setName("经典单开（我的副本）");
        existingClone.setLayout(FridgeLayout.CLASSIC);
        when(templateMapper.selectById(1L)).thenReturn(sys);
        when(templateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingClone);
        when(templateMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(existingClone));
        when(templateMapper.updateById(any(FridgeTemplate.class))).thenAnswer(inv -> 1);

        FridgeTemplateVO vo = service.activate(1L, 1L);

        assertEquals(99L, vo.getId());
        assertEquals(1L, vo.getOwnerId());
        assertTrue(vo.isDefault());
        assertFalse(vo.isSystem());
        // 不应触发新克隆的 insert
        verify(templateMapper, never()).insert(any(FridgeTemplate.class));
    }

    @Test
    void shouldFallbackToSystemWhenUserIdIsNull() {
        when(templateMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(systemTemplate(2L));

        FridgeTemplateVO vo = service.getDefault(null);

        assertEquals(2L, vo.getId());
        assertTrue(vo.isSystem());
    }

    @Test
    void shouldSwitchDefaultWithoutClearingNewTarget() {
        FridgeTemplate target = userTemplate(30L, 1L, false);
        FridgeTemplate oldDefault = userTemplate(15L, 1L, true);
        when(templateMapper.selectById(30L)).thenReturn(target);
        when(templateMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(target, oldDefault));
        when(templateMapper.updateById(any(FridgeTemplate.class))).thenAnswer(inv -> 1);

        FridgeTemplateVO vo = service.activate(30L, 1L);

        assertTrue(vo.isDefault());
        ArgumentCaptor<FridgeTemplate> captor = ArgumentCaptor.forClass(FridgeTemplate.class);
        verify(templateMapper, org.mockito.Mockito.atLeast(2)).updateById(captor.capture());
        // 旧 default 被清，新 default 被设；但新 default 的 update 不应把它自己置为 false
        List<FridgeTemplate> updates = captor.getAllValues();
        boolean oldCleared = updates.stream()
                .anyMatch(t -> t.getId().equals(15L) && Boolean.FALSE.equals(t.getIsDefault()));
        boolean newSet = updates.stream()
                .anyMatch(t -> t.getId().equals(30L) && Boolean.TRUE.equals(t.getIsDefault()));
        assertTrue(oldCleared);
        assertTrue(newSet);
    }
}
