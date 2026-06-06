package com.homecentral.fridge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homecentral.fridge.api.dto.FridgeCategoryRequest;
import com.homecentral.fridge.api.vo.FridgeCategoryVO;
import com.homecentral.fridge.entity.FridgeCategory;
import com.homecentral.fridge.mapper.FridgeCategoryMapper;
import com.homecentral.fridge.service.IFridgeCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FridgeCategoryServiceImpl implements IFridgeCategoryService {

    private final FridgeCategoryMapper categoryMapper;

    public FridgeCategoryServiceImpl(FridgeCategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<FridgeCategoryVO> listVisible(Long userId) {
        LambdaQueryWrapper<FridgeCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(FridgeCategory::getIsSystem, true)
                .or().eq(FridgeCategory::getCreatedBy, userId))
                .orderByAsc(FridgeCategory::getSortOrder)
                .orderByAsc(FridgeCategory::getId);
        return categoryMapper.selectList(wrapper).stream().map(this::toVO).toList();
    }

    @Override
    @Transactional
    public FridgeCategoryVO create(FridgeCategoryRequest request, Long userId) {
        assertNameAvailable(request.getName(), null, userId);

        FridgeCategory entity = new FridgeCategory();
        entity.setName(request.getName().trim());
        entity.setIcon(request.getIcon());
        entity.setColor(request.getColor());
        entity.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        entity.setIsSystem(false);
        entity.setCreatedBy(userId);
        OffsetDateTime now = OffsetDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        categoryMapper.insert(entity);
        return toVO(entity);
    }

    @Override
    @Transactional
    public FridgeCategoryVO update(Long id, FridgeCategoryRequest request, Long userId) {
        FridgeCategory existing = categoryMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("分类不存在");
        }
        if (Boolean.TRUE.equals(existing.getIsSystem())) {
            throw new RuntimeException("系统预置分类不可修改");
        }
        if (!userId.equals(existing.getCreatedBy())) {
            throw new RuntimeException("只能修改自己创建的分类");
        }
        assertNameAvailable(request.getName(), id, userId);

        existing.setName(request.getName().trim());
        existing.setIcon(request.getIcon());
        existing.setColor(request.getColor());
        if (request.getSortOrder() != null) {
            existing.setSortOrder(request.getSortOrder());
        }
        existing.setUpdatedAt(OffsetDateTime.now());
        categoryMapper.updateById(existing);
        return toVO(existing);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        FridgeCategory existing = categoryMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("分类不存在");
        }
        if (Boolean.TRUE.equals(existing.getIsSystem())) {
            throw new RuntimeException("系统预置分类不可删除");
        }
        if (!userId.equals(existing.getCreatedBy())) {
            throw new RuntimeException("只能删除自己创建的分类");
        }
        categoryMapper.deleteById(id);
    }

    @Override
    public List<FridgeCategoryVO> findAll() {
        LambdaQueryWrapper<FridgeCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(FridgeCategory::getSortOrder)
                .orderByAsc(FridgeCategory::getId);
        return categoryMapper.selectList(wrapper).stream().map(this::toVO).toList();
    }

    private void assertNameAvailable(String name, Long excludeId, Long userId) {
        if (name == null || name.isBlank()) {
            throw new RuntimeException("分类名称不能为空");
        }
        Set<String> existing = new HashSet<>();
        LambdaQueryWrapper<FridgeCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FridgeCategory::getCreatedBy, userId);
        if (excludeId != null) {
            wrapper.ne(FridgeCategory::getId, excludeId);
        }
        for (FridgeCategory c : categoryMapper.selectList(wrapper)) {
            existing.add(c.getName().trim());
        }
        if (existing.contains(name.trim())) {
            throw new RuntimeException("已存在同名分类");
        }
    }

    private FridgeCategoryVO toVO(FridgeCategory entity) {
        FridgeCategoryVO vo = new FridgeCategoryVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setIcon(entity.getIcon());
        vo.setColor(entity.getColor());
        vo.setSortOrder(entity.getSortOrder());
        vo.setSystem(Boolean.TRUE.equals(entity.getIsSystem()));
        vo.setCreatedBy(entity.getCreatedBy());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }
}
