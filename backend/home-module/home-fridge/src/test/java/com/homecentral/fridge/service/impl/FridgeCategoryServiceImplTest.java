package com.homecentral.fridge.service.impl;

import com.homecentral.fridge.api.dto.FridgeCategoryRequest;
import com.homecentral.fridge.api.vo.FridgeCategoryVO;
import com.homecentral.fridge.entity.FridgeCategory;
import com.homecentral.fridge.mapper.FridgeCategoryMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FridgeCategoryServiceImplTest {

    @Mock
    private FridgeCategoryMapper categoryMapper;

    @InjectMocks
    private FridgeCategoryServiceImpl service;

    private FridgeCategory categoryEntity(Long id, String name, boolean system, Long createdBy) {
        FridgeCategory c = new FridgeCategory();
        c.setId(id);
        c.setName(name);
        c.setIsSystem(system);
        c.setCreatedBy(createdBy);
        c.setSortOrder(10);
        c.setCreatedAt(OffsetDateTime.now());
        c.setUpdatedAt(OffsetDateTime.now());
        return c;
    }

    @Test
    void shouldListVisibleCategoriesIncludingSystemAndUser() {
        FridgeCategory sys = categoryEntity(1L, "蔬菜", true, null);
        FridgeCategory user = categoryEntity(2L, "烘焙", false, 1L);
        when(categoryMapper.selectList(any())).thenReturn(List.of(sys, user));

        List<FridgeCategoryVO> result = service.listVisible(1L);

        assertEquals(2, result.size());
        assertTrue(result.get(0).isSystem());
        assertFalse(result.get(1).isSystem());
    }

    @Test
    void shouldCreateUserCategoryWithDefaults() {
        FridgeCategoryRequest req = new FridgeCategoryRequest();
        req.setName("烘焙");
        req.setIcon("🍞");
        req.setColor("#F59E0B");
        req.setSortOrder(0);

        when(categoryMapper.selectList(any())).thenReturn(List.of());
        ArgumentCaptor<FridgeCategory> captor = ArgumentCaptor.forClass(FridgeCategory.class);
        when(categoryMapper.insert(captor.capture())).thenAnswer(inv -> {
            FridgeCategory c = inv.getArgument(0);
            c.setId(10L);
            return 1;
        });

        FridgeCategoryVO vo = service.create(req, 1L);

        verify(categoryMapper).insert(captor.capture());
        FridgeCategory saved = captor.getValue();
        assertEquals("烘焙", saved.getName());
        assertEquals(false, saved.getIsSystem());
        assertEquals(1L, saved.getCreatedBy());
        assertEquals(10L, vo.getId());
        assertFalse(vo.isSystem());
    }

    @Test
    void shouldThrowWhenCreatingDuplicateCategory() {
        FridgeCategoryRequest req = new FridgeCategoryRequest();
        req.setName("烘焙");

        FridgeCategory existing = categoryEntity(2L, "烘焙", false, 1L);
        when(categoryMapper.selectList(any())).thenReturn(List.of(existing));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.create(req, 1L));
        assertEquals("已存在同名分类", ex.getMessage());
        verify(categoryMapper, never()).insert(any(FridgeCategory.class));
    }

    @Test
    void shouldUpdateUserCategory() {
        FridgeCategoryRequest req = new FridgeCategoryRequest();
        req.setName("烘焙原料");
        req.setIcon("🥖");
        req.setColor("#FBBF24");

        FridgeCategory existing = categoryEntity(2L, "烘焙", false, 1L);
        when(categoryMapper.selectById(2L)).thenReturn(existing);
        when(categoryMapper.selectList(any())).thenReturn(List.of());
        ArgumentCaptor<FridgeCategory> captor = ArgumentCaptor.forClass(FridgeCategory.class);
        when(categoryMapper.updateById(captor.capture())).thenReturn(1);

        FridgeCategoryVO vo = service.update(2L, req, 1L);

        assertEquals("烘焙原料", vo.getName());
        verify(categoryMapper).updateById(existing);
    }

    @Test
    void shouldThrowWhenUpdateSystemCategory() {
        FridgeCategoryRequest req = new FridgeCategoryRequest();
        req.setName("蔬菜改");
        FridgeCategory sys = categoryEntity(1L, "蔬菜", true, null);
        when(categoryMapper.selectById(1L)).thenReturn(sys);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.update(1L, req, 1L));
        assertEquals("系统预置分类不可修改", ex.getMessage());
        verify(categoryMapper, never()).updateById(any(FridgeCategory.class));
    }

    @Test
    void shouldThrowWhenDeleteSystemCategory() {
        FridgeCategory sys = categoryEntity(1L, "蔬菜", true, null);
        when(categoryMapper.selectById(1L)).thenReturn(sys);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.delete(1L, 1L));
        assertEquals("系统预置分类不可删除", ex.getMessage());
        verify(categoryMapper, never()).deleteById(any(java.io.Serializable.class));
    }

    @Test
    void shouldDeleteUserCategory() {
        FridgeCategory user = categoryEntity(2L, "烘焙", false, 1L);
        when(categoryMapper.selectById(2L)).thenReturn(user);

        service.delete(2L, 1L);

        verify(categoryMapper).deleteById(2L);
    }
}
