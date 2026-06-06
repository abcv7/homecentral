package com.homecentral.fridge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homecentral.fridge.api.dto.FridgeTemplateRequest;
import com.homecentral.fridge.api.vo.FridgeTemplateVO;
import com.homecentral.fridge.entity.FridgeTemplate;
import com.homecentral.fridge.mapper.FridgeTemplateMapper;
import com.homecentral.fridge.service.IFridgeTemplateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class FridgeTemplateServiceImpl implements IFridgeTemplateService {

    private final FridgeTemplateMapper templateMapper;

    public FridgeTemplateServiceImpl(FridgeTemplateMapper templateMapper) {
        this.templateMapper = templateMapper;
    }

    @Override
    public List<FridgeTemplateVO> listVisible(Long userId) {
        LambdaQueryWrapper<FridgeTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(FridgeTemplate::getIsSystem, true)
                .or().eq(FridgeTemplate::getOwnerId, userId))
                .orderByDesc(FridgeTemplate::getIsSystem)
                .orderByDesc(FridgeTemplate::getIsDefault)
                .orderByAsc(FridgeTemplate::getId);
        return templateMapper.selectList(wrapper).stream().map(this::toVO).toList();
    }

    @Override
    public FridgeTemplateVO getDefault(Long userId) {
        if (userId != null) {
            LambdaQueryWrapper<FridgeTemplate> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(FridgeTemplate::getOwnerId, userId)
                    .eq(FridgeTemplate::getIsDefault, true)
                    .last("LIMIT 1");
            FridgeTemplate userDefault = templateMapper.selectOne(wrapper);
            if (userDefault != null) {
                return toVO(userDefault);
            }
        }
        LambdaQueryWrapper<FridgeTemplate> systemWrapper = new LambdaQueryWrapper<>();
        systemWrapper.eq(FridgeTemplate::getIsSystem, true)
                .orderByAsc(FridgeTemplate::getId)
                .last("LIMIT 1");
        FridgeTemplate system = templateMapper.selectOne(systemWrapper);
        if (system == null) {
            throw new RuntimeException("未找到任何冰箱模板，请联系管理员初始化");
        }
        return toVO(system);
    }

    @Override
    @Transactional
    public FridgeTemplateVO create(FridgeTemplateRequest request, Long userId) {
        assertNameAvailable(request.getName(), null, userId);

        FridgeTemplate entity = new FridgeTemplate();
        entity.setOwnerId(userId);
        entity.setName(request.getName().trim());
        entity.setLayout(request.getLayout());
        entity.setFridgeLayers(request.getFridgeLayers() != null ? request.getFridgeLayers() : 3);
        entity.setFreezerLayers(request.getFreezerLayers() != null ? request.getFreezerLayers() : 2);
        entity.setChillerLayers(request.getChillerLayers() != null ? request.getChillerLayers() : 0);
        entity.setDoorShelfCount(request.getDoorShelfCount() != null ? request.getDoorShelfCount() : 3);
        entity.setIsDefault(false);
        entity.setIsSystem(false);
        OffsetDateTime now = OffsetDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        templateMapper.insert(entity);
        return toVO(entity);
    }

    @Override
    @Transactional
    public FridgeTemplateVO update(Long id, FridgeTemplateRequest request, Long userId) {
        FridgeTemplate existing = templateMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("模板不存在");
        }
        if (Boolean.TRUE.equals(existing.getIsSystem())) {
            throw new RuntimeException("系统预置模板不可修改");
        }
        if (!userId.equals(existing.getOwnerId())) {
            throw new RuntimeException("只能修改自己创建的模板");
        }
        assertNameAvailable(request.getName(), id, userId);

        existing.setName(request.getName().trim());
        existing.setLayout(request.getLayout());
        existing.setFridgeLayers(request.getFridgeLayers() != null ? request.getFridgeLayers() : existing.getFridgeLayers());
        existing.setFreezerLayers(request.getFreezerLayers() != null ? request.getFreezerLayers() : existing.getFreezerLayers());
        existing.setChillerLayers(request.getChillerLayers() != null ? request.getChillerLayers() : existing.getChillerLayers());
        existing.setDoorShelfCount(request.getDoorShelfCount() != null ? request.getDoorShelfCount() : existing.getDoorShelfCount());
        existing.setUpdatedAt(OffsetDateTime.now());
        templateMapper.updateById(existing);
        return toVO(existing);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        FridgeTemplate existing = templateMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("模板不存在");
        }
        if (Boolean.TRUE.equals(existing.getIsSystem())) {
            throw new RuntimeException("系统预置模板不可删除");
        }
        if (!userId.equals(existing.getOwnerId())) {
            throw new RuntimeException("只能删除自己创建的模板");
        }
        if (Boolean.TRUE.equals(existing.getIsDefault())) {
            throw new RuntimeException("当前激活的默认模板不可删除，请先激活其他模板");
        }
        templateMapper.deleteById(id);
    }

    @Override
    @Transactional
    public FridgeTemplateVO activate(Long id, Long userId) {
        FridgeTemplate target = templateMapper.selectById(id);
        if (target == null) {
            throw new RuntimeException("模板不存在");
        }

        // 系统预置不允许直接激活（避免多人共享同一份 is_default 标记破坏唯一索引）
        // 改用 clone：基于系统预置创建一份用户专属模板并激活。
        // 同一系统预置在用户下应只对应一个副本；若已存在同名副本则复用。
        if (Boolean.TRUE.equals(target.getIsSystem())) {
            target = findOrCloneSystemTemplate(target, userId);
        } else if (!userId.equals(target.getOwnerId())) {
            throw new RuntimeException("只能激活自己创建的模板");
        }

        // 清空当前用户所有模板的 is_default
        LambdaQueryWrapper<FridgeTemplate> clearWrapper = new LambdaQueryWrapper<>();
        clearWrapper.eq(FridgeTemplate::getOwnerId, userId);
        List<FridgeTemplate> all = templateMapper.selectList(clearWrapper);
        OffsetDateTime now = OffsetDateTime.now();
        for (FridgeTemplate t : all) {
            if (!t.getId().equals(target.getId()) && Boolean.TRUE.equals(t.getIsDefault())) {
                t.setIsDefault(false);
                t.setUpdatedAt(now);
                templateMapper.updateById(t);
            }
        }

        target.setIsDefault(true);
        target.setUpdatedAt(now);
        templateMapper.updateById(target);
        return toVO(target);
    }

    /**
     * 查找或克隆系统预置模板：同源系统模板在用户下应仅有一份副本。
     * 若用户已存在同名（src.name + "（我的副本）"）的非系统模板，直接复用并回读；
     * 否则克隆一份并 selectById 回读，保证 ownerId / isDefault 真实落库。
     */
    private FridgeTemplate findOrCloneSystemTemplate(FridgeTemplate src, Long userId) {
        String cloneName = src.getName() + "（我的副本）";
        LambdaQueryWrapper<FridgeTemplate> nameQuery = new LambdaQueryWrapper<>();
        nameQuery.eq(FridgeTemplate::getOwnerId, userId)
                .eq(FridgeTemplate::getName, cloneName)
                .eq(FridgeTemplate::getIsSystem, false)
                .last("LIMIT 1");
        FridgeTemplate existing = templateMapper.selectOne(nameQuery);
        if (existing != null) {
            return existing;
        }
        FridgeTemplate clone = new FridgeTemplate();
        clone.setOwnerId(userId);
        clone.setName(cloneName);
        clone.setLayout(src.getLayout());
        clone.setFridgeLayers(src.getFridgeLayers());
        clone.setFreezerLayers(src.getFreezerLayers());
        clone.setChillerLayers(src.getChillerLayers());
        clone.setDoorShelfCount(src.getDoorShelfCount());
        clone.setIsDefault(false);
        clone.setIsSystem(false);
        OffsetDateTime now = OffsetDateTime.now();
        clone.setCreatedAt(now);
        clone.setUpdatedAt(now);
        templateMapper.insert(clone);
        FridgeTemplate reloaded = templateMapper.selectById(clone.getId());
        return reloaded != null ? reloaded : clone;
    }

    private void assertNameAvailable(String name, Long excludeId, Long userId) {
        if (name == null || name.isBlank()) {
            throw new RuntimeException("模板名称不能为空");
        }
        Set<String> existing = new HashSet<>();
        LambdaQueryWrapper<FridgeTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FridgeTemplate::getOwnerId, userId);
        if (excludeId != null) {
            wrapper.ne(FridgeTemplate::getId, excludeId);
        }
        for (FridgeTemplate t : templateMapper.selectList(wrapper)) {
            existing.add(t.getName().trim());
        }
        if (existing.contains(name.trim())) {
            throw new RuntimeException("已存在同名模板");
        }
    }

    private FridgeTemplateVO toVO(FridgeTemplate entity) {
        FridgeTemplateVO vo = new FridgeTemplateVO();
        vo.setId(entity.getId());
        vo.setOwnerId(entity.getOwnerId());
        vo.setName(entity.getName());
        vo.setLayout(entity.getLayout());
        vo.setFridgeLayers(entity.getFridgeLayers());
        vo.setFreezerLayers(entity.getFreezerLayers());
        vo.setChillerLayers(entity.getChillerLayers());
        vo.setDoorShelfCount(entity.getDoorShelfCount());
        vo.setDefault(Boolean.TRUE.equals(entity.getIsDefault()));
        vo.setSystem(Boolean.TRUE.equals(entity.getIsSystem()));
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}
