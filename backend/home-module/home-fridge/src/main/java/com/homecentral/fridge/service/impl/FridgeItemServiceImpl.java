package com.homecentral.fridge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.homecentral.fridge.api.dto.FridgeItemMoveRequest;
import com.homecentral.fridge.api.dto.FridgeItemRequest;
import com.homecentral.fridge.api.dto.FridgeMigrateRequest;
import com.homecentral.fridge.api.dto.FridgeQuickCreateRequest;
import com.homecentral.fridge.api.enums.FridgeItemSource;
import com.homecentral.fridge.api.enums.FridgeItemStatus;
import com.homecentral.fridge.api.enums.FridgeLayout;
import com.homecentral.fridge.api.enums.FridgeZone;
import com.homecentral.fridge.api.vo.FridgeCategoryVO;
import com.homecentral.fridge.api.vo.FridgeExpiringVO;
import com.homecentral.fridge.api.vo.FridgeItemVO;
import com.homecentral.fridge.api.vo.FridgeMigrateResultVO;
import com.homecentral.fridge.entity.FridgeCategory;
import com.homecentral.fridge.entity.FridgeItem;
import com.homecentral.fridge.mapper.FridgeCategoryMapper;
import com.homecentral.fridge.mapper.FridgeItemMapper;
import com.homecentral.fridge.service.IFridgeItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FridgeItemServiceImpl implements IFridgeItemService {

    private final FridgeItemMapper itemMapper;
    private final FridgeCategoryMapper categoryMapper;

    public FridgeItemServiceImpl(FridgeItemMapper itemMapper, FridgeCategoryMapper categoryMapper) {
        this.itemMapper = itemMapper;
        this.categoryMapper = categoryMapper;
    }

    @Override
    @Transactional
    public FridgeItemVO create(FridgeItemRequest request, Long userId) {
        FridgeItem entity = toEntity(request, userId);
        OffsetDateTime now = OffsetDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        if (entity.getStatus() == null) {
            entity.setStatus(FridgeItemStatus.ACTIVE);
        }
        if (entity.getSource() == null) {
            entity.setSource(FridgeItemSource.MANUAL);
        }
        itemMapper.insert(entity);
        return toVO(entity);
    }

    @Override
    @Transactional
    public List<FridgeItemVO> batchCreate(List<FridgeItemRequest> requests, Long userId) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }
        OffsetDateTime now = OffsetDateTime.now();
        List<FridgeItem> entities = requests.stream().map(req -> {
            FridgeItem entity = toEntity(req, userId);
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);
            if (entity.getStatus() == null) {
                entity.setStatus(FridgeItemStatus.ACTIVE);
            }
            if (entity.getSource() == null) {
                entity.setSource(FridgeItemSource.MANUAL);
            }
            return entity;
        }).toList();
        for (FridgeItem e : entities) {
            itemMapper.insert(e);
        }
        return entities.stream().map(this::toVO).toList();
    }

    @Override
    @Transactional
    public FridgeItemVO update(Long id, FridgeItemRequest request, Long userId) {
        FridgeItem existing = itemMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("食材不存在");
        }
        if (FridgeItemStatus.CONSUMED.equals(existing.getStatus())) {
            throw new RuntimeException("已消耗的食材不可修改");
        }
        if (userId != null && existing.getCreatedBy() != null && !userId.equals(existing.getCreatedBy())) {
            throw new RuntimeException("只能修改自己添加的食材");
        }

        existing.setName(request.getName());
        existing.setZone(request.getZone());
        existing.setSubZone(request.getSubZone());
        existing.setCategoryId(request.getCategoryId());
        existing.setQuantity(request.getQuantity());
        existing.setUnit(request.getUnit());
        existing.setPurchaseDate(request.getPurchaseDate());
        existing.setExpiryDate(request.getExpiryDate());
        existing.setImageUrl(request.getImageUrl());
        existing.setNotes(request.getNotes());
        if (request.getSource() != null) {
            existing.setSource(request.getSource());
        }
        existing.setUpdatedAt(OffsetDateTime.now());
        itemMapper.updateById(existing);
        return toVO(existing);
    }

    @Override
    public FridgeItemVO getById(Long id, Long userId) {
        FridgeItem entity = itemMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("食材不存在");
        }
        if (userId != null && entity.getCreatedBy() != null && !userId.equals(entity.getCreatedBy())) {
            throw new RuntimeException("无权访问此食材");
        }
        return toVO(entity);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        FridgeItem existing = itemMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("食材不存在");
        }
        if (!FridgeItemStatus.ACTIVE.equals(existing.getStatus())) {
            throw new RuntimeException("仅在库的食材可删除");
        }
        if (userId != null && existing.getCreatedBy() != null && !userId.equals(existing.getCreatedBy())) {
            throw new RuntimeException("只能删除自己添加的食材");
        }
        itemMapper.deleteById(id);
    }

    @Override
    @Transactional
    public FridgeItemVO consume(Long id, Long userId) {
        FridgeItem existing = itemMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("食材不存在");
        }
        if (!FridgeItemStatus.ACTIVE.equals(existing.getStatus())) {
            throw new RuntimeException("仅在库的食材可标记消耗");
        }
        if (userId != null && existing.getCreatedBy() != null && !userId.equals(existing.getCreatedBy())) {
            throw new RuntimeException("只能标记自己添加的食材");
        }
        existing.setStatus(FridgeItemStatus.CONSUMED);
        existing.setConsumedAt(OffsetDateTime.now());
        existing.setUpdatedAt(OffsetDateTime.now());
        itemMapper.updateById(existing);
        return toVO(existing);
    }

    @Override
    @Transactional
    public FridgeItemVO consumeOne(Long id, Long userId) {
        FridgeItem existing = itemMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("食材不存在");
        }
        if (!FridgeItemStatus.ACTIVE.equals(existing.getStatus())) {
            throw new RuntimeException("仅在库的食材可标记消耗");
        }
        if (userId != null && existing.getCreatedBy() != null && !userId.equals(existing.getCreatedBy())) {
            throw new RuntimeException("只能标记自己添加的食材");
        }
        BigDecimal current = existing.getQuantity() == null ? BigDecimal.ONE : existing.getQuantity();
        if (current.compareTo(BigDecimal.ONE) > 0) {
            existing.setQuantity(current.subtract(BigDecimal.ONE));
        } else {
            existing.setStatus(FridgeItemStatus.CONSUMED);
            existing.setConsumedAt(OffsetDateTime.now());
        }
        existing.setUpdatedAt(OffsetDateTime.now());
        itemMapper.updateById(existing);
        return toVO(existing);
    }

    @Override
    @Transactional
    public FridgeItemVO discard(Long id, Long userId) {
        FridgeItem existing = itemMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("食材不存在");
        }
        if (!FridgeItemStatus.ACTIVE.equals(existing.getStatus())) {
            throw new RuntimeException("仅在库的食材可标记丢弃");
        }
        if (userId != null && existing.getCreatedBy() != null && !userId.equals(existing.getCreatedBy())) {
            throw new RuntimeException("只能标记自己添加的食材");
        }
        existing.setStatus(FridgeItemStatus.DISCARDED);
        existing.setConsumedAt(OffsetDateTime.now());
        existing.setUpdatedAt(OffsetDateTime.now());
        itemMapper.updateById(existing);
        return toVO(existing);
    }

    @Override
    public List<FridgeItemVO> list(Long userId, FridgeZone zone, Long categoryId, FridgeItemStatus status, boolean includePending) {
        LambdaQueryWrapper<FridgeItem> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(FridgeItem::getCreatedBy, userId);
        }
        if (zone != null) {
            wrapper.eq(FridgeItem::getZone, zone);
        }
        if (categoryId != null) {
            wrapper.eq(FridgeItem::getCategoryId, categoryId);
        }
        if (status != null) {
            wrapper.eq(FridgeItem::getStatus, status);
        } else if (includePending) {
            // 采购篮视图：PENDING + ACTIVE 都返回
            wrapper.in(FridgeItem::getStatus, FridgeItemStatus.PENDING, FridgeItemStatus.ACTIVE);
        } else {
            wrapper.eq(FridgeItem::getStatus, FridgeItemStatus.ACTIVE);
        }
        wrapper.orderByAsc(FridgeItem::getExpiryDate)
                .orderByDesc(FridgeItem::getCreatedAt);

        List<FridgeItem> items = itemMapper.selectList(wrapper);
        if (items.isEmpty()) {
            return List.of();
        }
        Map<Long, FridgeCategory> categoryMap = loadCategoryMap(items);
        return items.stream().map(e -> toVO(e, categoryMap)).toList();
    }

    @Override
    public FridgeExpiringVO expiringStats(Long userId, int days) {
        if (days <= 0) {
            days = 3;
        }
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(days);

        LambdaQueryWrapper<FridgeItem> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.eq(FridgeItem::getStatus, FridgeItemStatus.ACTIVE);
        if (userId != null) {
            activeWrapper.eq(FridgeItem::getCreatedBy, userId);
        }
        activeWrapper.isNotNull(FridgeItem::getExpiryDate);
        List<FridgeItem> items = itemMapper.selectList(activeWrapper);

        long expired = 0;
        long expiring = 0;
        for (FridgeItem item : items) {
            if (item.getExpiryDate().isBefore(today)) {
                expired++;
            } else if (!item.getExpiryDate().isAfter(threshold)) {
                expiring++;
            }
        }
        return new FridgeExpiringVO(expired, expiring, days);
    }

    private FridgeItem toEntity(FridgeItemRequest req, Long userId) {
        FridgeItem entity = new FridgeItem();
        entity.setName(req.getName());
        entity.setZone(req.getZone());
        entity.setSubZone(req.getSubZone());
        entity.setCategoryId(req.getCategoryId());
        entity.setQuantity(req.getQuantity());
        entity.setUnit(req.getUnit());
        entity.setPurchaseDate(req.getPurchaseDate());
        entity.setExpiryDate(req.getExpiryDate());
        entity.setImageUrl(req.getImageUrl());
        entity.setSource(req.getSource());
        entity.setNotes(req.getNotes());
        entity.setCreatedBy(userId);
        return entity;
    }

    @Override
    @Transactional
    public FridgeItemVO move(Long id, FridgeItemMoveRequest request, Long userId) {
        FridgeItem existing = itemMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("食材不存在");
        }
        if (userId != null && existing.getCreatedBy() != null && !userId.equals(existing.getCreatedBy())) {
            throw new RuntimeException("只能移动自己添加的食材");
        }
        if (FridgeItemStatus.CONSUMED.equals(existing.getStatus())) {
            throw new RuntimeException("已消耗的食材不可移动");
        }
        if (FridgeItemStatus.DISCARDED.equals(existing.getStatus())) {
            throw new RuntimeException("已丢弃的食材不可移动");
        }

        OffsetDateTime now = OffsetDateTime.now();

        if (request == null || (request.getZone() == null && (request.getSubZone() == null || request.getSubZone().isBlank()))) {
            // 退回采购篮
            existing.setStatus(FridgeItemStatus.PENDING);
            existing.setZone(null);
            existing.setSubZone(null);
        } else {
            // 归位到具体温区
            existing.setZone(request.getZone());
            existing.setSubZone(request.getSubZone());
            // 采购篮 -> 库内的转换：状态自动激活
            if (FridgeItemStatus.PENDING.equals(existing.getStatus())) {
                existing.setStatus(FridgeItemStatus.ACTIVE);
            }
        }
        existing.setUpdatedAt(now);
        itemMapper.updateById(existing);
        return toVO(existing);
    }

    @Override
    @Transactional
    public FridgeItemVO quickCreate(FridgeQuickCreateRequest request, Long userId) {
        FridgeItem entity = new FridgeItem();
        entity.setName(request.getName().trim());
        entity.setCategoryId(request.getCategoryId());
        // 数量：未传或 <=0 时默认为 1
        BigDecimal qty = request.getQuantity();
        entity.setQuantity(qty == null || qty.compareTo(BigDecimal.ZERO) <= 0 ? BigDecimal.ONE : qty);
        entity.setPurchaseDate(parseDate(request.getPurchaseDate(), LocalDate.now()));
        entity.setExpiryDate(parseDate(request.getExpiryDate(), null));
        entity.setSource(FridgeItemSource.MANUAL);

        boolean toBasket = request.getZone() == null
                || request.getSubZone() == null
                || request.getSubZone().isBlank();
        if (toBasket) {
            entity.setStatus(FridgeItemStatus.PENDING);
            entity.setZone(null);
            entity.setSubZone(null);
        } else {
            entity.setStatus(FridgeItemStatus.ACTIVE);
            entity.setZone(request.getZone());
            entity.setSubZone(request.getSubZone());
        }
        entity.setCreatedBy(userId);
        OffsetDateTime now = OffsetDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        itemMapper.insert(entity);
        return toVO(entity);
    }

    private LocalDate parseDate(String raw, LocalDate fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("日期格式不正确，应为 yyyy-MM-dd: " + raw);
        }
    }

    @Override
    @Transactional
    public FridgeMigrateResultVO migrateOnLayoutChange(FridgeMigrateRequest request, Long userId) {
        if (request == null || request.getLayout() == null) {
            throw new RuntimeException("layout 不能为空");
        }
        if (userId == null) {
            return new FridgeMigrateResultVO(0, 0);
        }
        int fridgeLayers = request.getFridgeLayers() == null ? 0 : request.getFridgeLayers();
        int freezerLayers = request.getFreezerLayers() == null ? 0 : request.getFreezerLayers();
        int chillerLayers = request.getChillerLayers() == null ? 0 : request.getChillerLayers();
        int doorShelfCount = request.getDoorShelfCount() == null ? 0 : request.getDoorShelfCount();
        FridgeLayout layout = request.getLayout();

        LambdaQueryWrapper<FridgeItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FridgeItem::getCreatedBy, userId)
                .eq(FridgeItem::getStatus, FridgeItemStatus.ACTIVE);
        List<FridgeItem> items = itemMapper.selectList(wrapper);
        if (items.isEmpty()) {
            return new FridgeMigrateResultVO(0, 0);
        }

        List<Long> toMigrate = new ArrayList<>();
        for (FridgeItem it : items) {
            if (needsMigration(it, layout, fridgeLayers, freezerLayers, chillerLayers, doorShelfCount)) {
                toMigrate.add(it.getId());
            }
        }
        if (toMigrate.isEmpty()) {
            return new FridgeMigrateResultVO(items.size(), 0);
        }

        UpdateWrapper<FridgeItem> update = new UpdateWrapper<>();
        update.in("id", toMigrate)
                .set("status", FridgeItemStatus.PENDING.name())
                .set("zone", null)
                .set("sub_zone", null)
                .set("updated_at", OffsetDateTime.now());
        int affected = itemMapper.update(null, update);
        return new FridgeMigrateResultVO(items.size(), affected);
    }

    /**
     * 判断一条食材在新方案下是否需要迁出。
     * 规则：
     * <ul>
     *     <li>zone=null：本身就在采购篮，跳过</li>
     *     <li>subZone 为规范编码 (ZONE-LN) 且 N 超过新区层数 → 迁出</li>
     *     <li>subZone 为规范编码 (DOOR-LEFT-LN) 且 layout != SIDE_BY_SIDE → 迁出（THREE_DOOR 左门属于冷藏）</li>
     *     <li>subZone 为规范编码 (DOOR-RIGHT-LN) 且 layout ∉ {SIDE_BY_SIDE, THREE_DOOR} → 迁出</li>
     *     <li>subZone 为规范编码 (CHILLER-LN) 且 layout != THREE_DOOR → 迁出</li>
     *     <li>subZone 为门编码且 N 超过新门搁板数 → 迁出</li>
     *     <li>中文/自由文本 subZone：按 zone 本身是否还存在判断（zone=REFRIGERATED 永远兼容；FROZEN 同理；旧 zone 字段）</li>
     * </ul>
     */
    private boolean needsMigration(FridgeItem item, FridgeLayout layout,
                                   int fridgeLayers, int freezerLayers,
                                   int chillerLayers, int doorShelfCount) {
        FridgeZone zone = item.getZone();
        if (zone == null) {
            return false;
        }
        String sub = item.getSubZone();
        if (sub == null || sub.isBlank()) {
            return false;
        }
        String s = sub.trim().toUpperCase();

        // 严格编码 (ZONE|DOOR-SIDE)-L<digits>
        // 使用 Matcher 捕获组解析：避免 "-L" 出现在 ZONE/DOOR 名称内时 split 错位
        var zoneLayer = java.util.regex.Pattern.compile("^(REFRIGERATED|FROZEN|CHILLER)-L(\\d+)$").matcher(s);
        if (zoneLayer.matches()) {
            String z = zoneLayer.group(1);
            int n = Integer.parseInt(zoneLayer.group(2));
            return switch (z) {
                case "REFRIGERATED" -> n > fridgeLayers;
                case "FROZEN" -> n > freezerLayers;
                case "CHILLER" -> layout != FridgeLayout.THREE_DOOR || n > chillerLayers;
                default -> true;
            };
        }
        var doorLayer = java.util.regex.Pattern.compile("^DOOR-(LEFT|RIGHT)-L(\\d+)$").matcher(s);
        if (doorLayer.matches()) {
            String side = doorLayer.group(1);
            int n = Integer.parseInt(doorLayer.group(2));
            if (n > doorShelfCount) {
                return true;
            }
            if ("LEFT".equals(side)) {
                return layout != FridgeLayout.SIDE_BY_SIDE;
            }
            return layout != FridgeLayout.SIDE_BY_SIDE && layout != FridgeLayout.THREE_DOOR;
        }

        // 中文/自由文本：仅按 zone 是否仍存在判断
        // 4 种门型都含 REFRIGERATED + FROZEN；CHILLER 仅 THREE_DOOR
        // zone 字段已限定枚举（REFRIGERATED / FROZEN），故无 zone 层面的兼容性问题
        return false;
    }

    private Map<Long, FridgeCategory> loadCategoryMap(List<FridgeItem> items) {
        Set<Long> ids = items.stream()
                .map(FridgeItem::getCategoryId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<FridgeCategory> categories = categoryMapper.selectBatchIds(ids);
        Map<Long, FridgeCategory> map = new HashMap<>();
        for (FridgeCategory c : categories) {
            map.put(c.getId(), c);
        }
        return map;
    }

    private FridgeItemVO toVO(FridgeItem entity) {
        return toVO(entity, loadCategoryMap(List.of(entity)));
    }

    private FridgeItemVO toVO(FridgeItem entity, Map<Long, FridgeCategory> categoryMap) {
        FridgeItemVO vo = new FridgeItemVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setZone(entity.getZone());
        vo.setSubZone(entity.getSubZone());
        vo.setCategoryId(entity.getCategoryId());
        FridgeCategory cat = entity.getCategoryId() != null ? categoryMap.get(entity.getCategoryId()) : null;
        if (cat != null) {
            vo.setCategoryName(cat.getName());
            vo.setCategoryIcon(cat.getIcon());
            vo.setCategoryColor(cat.getColor());
        }
        vo.setQuantity(entity.getQuantity());
        vo.setUnit(entity.getUnit());
        vo.setPurchaseDate(entity.getPurchaseDate());
        vo.setExpiryDate(entity.getExpiryDate());
        if (entity.getExpiryDate() != null) {
            vo.setDaysToExpiry(ChronoUnit.DAYS.between(LocalDate.now(), entity.getExpiryDate()));
        }
        vo.setImageUrl(entity.getImageUrl());
        vo.setSource(entity.getSource());
        vo.setStatus(entity.getStatus());
        vo.setNotes(entity.getNotes());
        vo.setConsumedAt(entity.getConsumedAt());
        vo.setCreatedBy(entity.getCreatedBy());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}
