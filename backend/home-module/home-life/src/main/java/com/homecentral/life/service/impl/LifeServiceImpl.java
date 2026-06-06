package com.homecentral.life.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.homecentral.life.service.ILifeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class LifeServiceImpl implements ILifeService {

    private final ShoppingMemoMapper shoppingMemoMapper;
    private final AnniversaryMapper anniversaryMapper;
    private final ReminderRuleMapper reminderRuleMapper;

    public LifeServiceImpl(ShoppingMemoMapper shoppingMemoMapper, AnniversaryMapper anniversaryMapper,
                           ReminderRuleMapper reminderRuleMapper) {
        this.shoppingMemoMapper = shoppingMemoMapper;
        this.anniversaryMapper = anniversaryMapper;
        this.reminderRuleMapper = reminderRuleMapper;
    }

    @Override
    @Transactional
    public ShoppingMemoVO createShoppingMemo(ShoppingMemoCreateRequest request, Long userId) {
        ShoppingMemo memo = new ShoppingMemo();
        memo.setItemName(request.getItemName());
        memo.setNote(request.getNote());
        memo.setPurchased(false);
        memo.setCreatedBy(userId);
        memo.setCreatedAt(OffsetDateTime.now());
        memo.setUpdatedAt(OffsetDateTime.now());
        shoppingMemoMapper.insert(memo);
        return toShoppingMemoVO(memo);
    }

    @Override
    public List<ShoppingMemoVO> listShoppingMemos(Long userId) {
        LambdaQueryWrapper<ShoppingMemo> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(ShoppingMemo::getCreatedBy, userId);
        }
        wrapper.orderByDesc(ShoppingMemo::getCreatedAt);
        return shoppingMemoMapper.selectList(wrapper).stream().map(this::toShoppingMemoVO).toList();
    }

    @Override
    @Transactional
    public ShoppingMemoVO togglePurchased(Long id, Long userId) {
        ShoppingMemo memo = shoppingMemoMapper.selectById(id);
        if (memo == null) {
            throw new RuntimeException("购物项不存在");
        }
        memo.setPurchased(!memo.getPurchased());
        memo.setUpdatedAt(OffsetDateTime.now());
        shoppingMemoMapper.updateById(memo);
        return toShoppingMemoVO(memo);
    }

    @Override
    @Transactional
    public AnniversaryVO createAnniversary(AnniversaryCreateRequest request, Long userId) {
        Anniversary ann = new Anniversary();
        ann.setTitle(request.getTitle());
        ann.setEventDate(request.getEventDate());
        ann.setRemindBeforeDays(request.getRemindBeforeDays() != null ? request.getRemindBeforeDays() : 0);
        ann.setCreatedBy(userId);
        ann.setCreatedAt(OffsetDateTime.now());
        ann.setUpdatedAt(OffsetDateTime.now());
        anniversaryMapper.insert(ann);
        return toAnniversaryVO(ann);
    }

    @Override
    public List<AnniversaryVO> listAnniversaries(Long userId) {
        LambdaQueryWrapper<Anniversary> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(Anniversary::getCreatedBy, userId);
        }
        wrapper.orderByAsc(Anniversary::getEventDate);
        return anniversaryMapper.selectList(wrapper).stream().map(this::toAnniversaryVO).toList();
    }

    @Override
    @Transactional
    public ReminderRuleVO createReminderRule(ReminderRuleCreateRequest request, Long userId) {
        ReminderRule rule = new ReminderRule();
        rule.setTitle(request.getTitle());
        rule.setContent(request.getContent());
        rule.setCronExpression(request.getCronExpression());
        rule.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
        rule.setCreatedBy(userId);
        rule.setCreatedAt(OffsetDateTime.now());
        rule.setUpdatedAt(OffsetDateTime.now());
        reminderRuleMapper.insert(rule);
        return toReminderRuleVO(rule);
    }

    @Override
    public List<ReminderRuleVO> listReminderRules(Long userId) {
        LambdaQueryWrapper<ReminderRule> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(ReminderRule::getCreatedBy, userId);
        }
        wrapper.orderByDesc(ReminderRule::getCreatedAt);
        return reminderRuleMapper.selectList(wrapper).stream().map(this::toReminderRuleVO).toList();
    }

    @Override
    @Transactional
    public ReminderRuleVO toggleEnabled(Long id, Long userId) {
        ReminderRule rule = reminderRuleMapper.selectById(id);
        if (rule == null) {
            throw new RuntimeException("提醒规则不存在");
        }
        rule.setEnabled(!rule.getEnabled());
        rule.setUpdatedAt(OffsetDateTime.now());
        reminderRuleMapper.updateById(rule);
        return toReminderRuleVO(rule);
    }

    private ShoppingMemoVO toShoppingMemoVO(ShoppingMemo memo) {
        ShoppingMemoVO vo = new ShoppingMemoVO();
        vo.setId(memo.getId());
        vo.setItemName(memo.getItemName());
        vo.setNote(memo.getNote());
        vo.setPurchased(memo.getPurchased());
        return vo;
    }

    private AnniversaryVO toAnniversaryVO(Anniversary ann) {
        AnniversaryVO vo = new AnniversaryVO();
        vo.setId(ann.getId());
        vo.setTitle(ann.getTitle());
        vo.setEventDate(ann.getEventDate());
        vo.setRemindBeforeDays(ann.getRemindBeforeDays());
        return vo;
    }

    private ReminderRuleVO toReminderRuleVO(ReminderRule rule) {
        ReminderRuleVO vo = new ReminderRuleVO();
        vo.setId(rule.getId());
        vo.setTitle(rule.getTitle());
        vo.setContent(rule.getContent());
        vo.setCronExpression(rule.getCronExpression());
        vo.setEnabled(rule.getEnabled());
        vo.setLastTriggeredAt(rule.getLastTriggeredAt());
        return vo;
    }
}
