package com.homecentral.fridge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homecentral.auth.api.feign.MemberEmailClient;
import com.homecentral.common.model.Result;
import com.homecentral.fridge.api.dto.FridgeShoppingConfirmRequest;
import com.homecentral.fridge.api.dto.FridgeShoppingItem;
import com.homecentral.fridge.api.enums.FridgeItemStatus;
import com.homecentral.fridge.api.vo.FridgeShoppingHistoryVO;
import com.homecentral.friend.api.feign.FriendClient;
import com.homecentral.fridge.entity.FridgeShoppingHistory;
import com.homecentral.fridge.mapper.FridgeItemMapper;
import com.homecentral.fridge.mapper.FridgeShoppingHistoryMapper;
import com.homecentral.fridge.service.IShoppingService;
import com.homecentral.notification.api.email.PurchaseItem;
import com.homecentral.notification.api.email.PurchaseNoticeEmail;
import com.homecentral.notification.api.email.PurchaseNoticeEmail.Variant;
import com.homecentral.notification.api.feign.MailClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ShoppingServiceImpl implements IShoppingService {

    private static final Logger log = LoggerFactory.getLogger(ShoppingServiceImpl.class);

    private final FridgeShoppingHistoryMapper historyMapper;
    private final FridgeItemMapper itemMapper;
    private final FriendClient friendClient;
    private final MemberEmailClient memberEmailClient;
    private final MailClient mailClient;

    public ShoppingServiceImpl(FridgeShoppingHistoryMapper historyMapper,
                               FridgeItemMapper itemMapper,
                               FriendClient friendClient,
                               MemberEmailClient memberEmailClient,
                               MailClient mailClient) {
        this.historyMapper = historyMapper;
        this.itemMapper = itemMapper;
        this.friendClient = friendClient;
        this.memberEmailClient = memberEmailClient;
        this.mailClient = mailClient;
    }

    @Override
    @Transactional
    public FridgeShoppingHistoryVO confirm(FridgeShoppingConfirmRequest request, Long userId) {
        if (userId == null) {
            throw new RuntimeException("需要登录");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("采购篮为空");
        }

        String batchId = java.util.UUID.randomUUID().toString();
        OffsetDateTime now = OffsetDateTime.now();
        List<FridgeShoppingHistory> rows = new ArrayList<>();
        for (FridgeShoppingItem item : request.getItems()) {
            FridgeShoppingHistory h = new FridgeShoppingHistory();
            h.setUserId(userId);
            h.setBatchId(batchId);
            h.setName(item.getName());
            h.setCategoryId(item.getCategoryId());
            h.setQuantity(item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0
                    ? BigDecimal.ONE : item.getQuantity());
            h.setUnit(item.getUnit());
            h.setSource(item.getSource() == null ? "MANUAL" : item.getSource());
            h.setPartnerEmail(request.getPartnerEmail());
            h.setEmailSent(Boolean.FALSE);
            h.setPurchasedAt(now);
            h.setCreatedAt(now);
            rows.add(h);
        }
        for (FridgeShoppingHistory row : rows) {
            historyMapper.insert(row);
        }

        clearBasketItems(request.getBasketItemIds(), userId);

        boolean anySent = false;
        List<PurchaseItem> items = toPurchaseItems(rows);
        if (request.getPartnerEmail() != null && !request.getPartnerEmail().isBlank()) {
            anySent |= sendPurchaseEmail(
                new PurchaseNoticeEmail(
                    request.getPartnerEmail(),
                    Variant.PARTNER,
                    null,
                    null,
                    null,
                    items));
        }
        if (request.getGroupId() != null) {
            anySent |= sendGroupEmail(userId, request.getGroupId(), items);
        }
        if (anySent) {
            for (FridgeShoppingHistory row : rows) {
                row.setEmailSent(Boolean.TRUE);
                historyMapper.updateById(row);
            }
        }
        Boolean emailSent = anySent ? Boolean.TRUE : Boolean.FALSE;

        return toVO(batchId, now, request.getPartnerEmail(), emailSent, rows);
    }

    /**
     * 清空采购篮：仅删除当前用户且状态为 PENDING 的 fridge_item 记录。
     * 与 confirm() 处于同一事务，任一异常都会回滚 history 插入，保证一致性。
     * 防护：
     *   - basketItemIds 为空 → 不清空
     *   - 非 PENDING 状态 → 自动跳过（即便 ID 属于其他用户或已移入库内，也不会被误删）
     *   - 不属于当前用户 → 不会进 IN 列表
     */
    private void clearBasketItems(List<Long> basketItemIds, Long userId) {
        if (basketItemIds == null || basketItemIds.isEmpty()) {
            return;
        }
        LambdaQueryWrapper<com.homecentral.fridge.entity.FridgeItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(com.homecentral.fridge.entity.FridgeItem::getCreatedBy, userId)
                .eq(com.homecentral.fridge.entity.FridgeItem::getStatus, FridgeItemStatus.PENDING)
                .in(com.homecentral.fridge.entity.FridgeItem::getId, basketItemIds);
        int cleared = itemMapper.delete(wrapper);
        if (cleared > 0) {
            log.info("[shopping-confirm] 清空采购篮 userId={} count={}", userId, cleared);
        }
    }

    private boolean sendGroupEmail(Long userId, Long groupId, List<PurchaseItem> items) {
        try {
            Result<List<Long>> memberRes = friendClient.getGroupMembers(groupId);
            if (memberRes == null || !memberRes.isSuccess() || memberRes.getData() == null) {
                log.info("[shopping-confirm] group={} 成员列表为空", groupId);
                return false;
            }
            Set<Long> userIds = new HashSet<>(memberRes.getData());
            userIds.remove(userId);
            if (userIds.isEmpty()) {
                return false;
            }
            boolean sent = false;
            for (Long targetId : userIds) {
                String email = fetchEmail(targetId);
                if (email == null || email.isBlank()) {
                    continue;
                }
                sent |= sendPurchaseEmail(
                    new PurchaseNoticeEmail(
                        email,
                        Variant.GROUP,
                        null,
                        null,
                        null,
                        items));
            }
            return sent;
        } catch (Exception e) {
            log.warn("[shopping-confirm] 组邮件失败 group={} ({})", groupId, e.getMessage());
            return false;
        }
    }

    private boolean sendPurchaseEmail(PurchaseNoticeEmail email) {
        try {
            mailClient.sendMessage(email);
            return true;
        } catch (Exception e) {
            log.warn("[shopping-confirm] 邮件失败 to={} ({})", email.getTo(), e.getMessage());
            return false;
        }
    }

    private String fetchEmail(Long userId) {
        try {
            Result<String> r = memberEmailClient.getMemberEmail(userId);
            if (r != null && r.isSuccess() && r.getData() != null) {
                return r.getData();
            }
        } catch (Exception e) {
            log.warn("[shopping-confirm] 查询邮箱失败 userId={} ({})", userId, e.getMessage());
        }
        return null;
    }

    private List<PurchaseItem> toPurchaseItems(List<FridgeShoppingHistory> rows) {
        if (rows == null) {
            return List.of();
        }
        List<PurchaseItem> items = new ArrayList<>(rows.size());
        for (FridgeShoppingHistory r : rows) {
            int qty = r.getQuantity() == null ? 1 : r.getQuantity().intValue();
            items.add(new PurchaseItem(r.getName(), qty, r.getUnit()));
        }
        return items;
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    @Override
    public List<FridgeShoppingHistoryVO> listHistory(Long userId, int limit) {
        if (userId == null) {
            return List.of();
        }
        LambdaQueryWrapper<FridgeShoppingHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FridgeShoppingHistory::getUserId, userId)
                .orderByDesc(FridgeShoppingHistory::getPurchasedAt)
                .last("LIMIT " + Math.max(1, Math.min(limit, 200)));
        List<FridgeShoppingHistory> rows = historyMapper.selectList(wrapper);
        return groupByBatch(rows);
    }

    @Override
    public FridgeShoppingHistoryVO getBatch(String batchId, Long userId) {
        if (userId == null) {
            throw new RuntimeException("需要登录");
        }
        LambdaQueryWrapper<FridgeShoppingHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FridgeShoppingHistory::getUserId, userId)
                .eq(FridgeShoppingHistory::getBatchId, batchId)
                .orderByAsc(FridgeShoppingHistory::getId);
        List<FridgeShoppingHistory> rows = historyMapper.selectList(wrapper);
        if (rows.isEmpty()) {
            return null;
        }
        FridgeShoppingHistory first = rows.get(0);
        return toVO(batchId, first.getPurchasedAt(), first.getPartnerEmail(),
                first.getEmailSent(), rows);
    }

    @Override
    public List<FridgeShoppingItem> reuseBatch(String batchId, Long userId) {
        if (userId == null) {
            throw new RuntimeException("需要登录");
        }
        LambdaQueryWrapper<FridgeShoppingHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FridgeShoppingHistory::getUserId, userId)
                .eq(FridgeShoppingHistory::getBatchId, batchId)
                .orderByAsc(FridgeShoppingHistory::getId);
        List<FridgeShoppingHistory> rows = historyMapper.selectList(wrapper);
        return rows.stream().map(this::toItem).collect(Collectors.toList());
    }

    @Override
    public FridgeShoppingItem reuseOne(Long historyId, Long userId) {
        if (userId == null) {
            throw new RuntimeException("需要登录");
        }
        FridgeShoppingHistory row = historyMapper.selectById(historyId);
        if (row == null || !userId.equals(row.getUserId())) {
            throw new RuntimeException("历史条目不存在");
        }
        return toItem(row);
    }

    private FridgeShoppingItem toItem(FridgeShoppingHistory row) {
        FridgeShoppingItem item = new FridgeShoppingItem();
        item.setName(row.getName());
        item.setCategoryId(row.getCategoryId());
        item.setQuantity(row.getQuantity());
        item.setUnit(row.getUnit());
        item.setSource(row.getSource());
        return item;
    }

    private List<FridgeShoppingHistoryVO> groupByBatch(List<FridgeShoppingHistory> rows) {
        Map<String, List<FridgeShoppingHistory>> map = new LinkedHashMap<>();
        for (FridgeShoppingHistory row : rows) {
            map.computeIfAbsent(row.getBatchId(), k -> new ArrayList<>()).add(row);
        }
        List<FridgeShoppingHistoryVO> result = new ArrayList<>();
        for (Map.Entry<String, List<FridgeShoppingHistory>> entry : map.entrySet()) {
            FridgeShoppingHistory first = entry.getValue().get(0);
            result.add(toVO(entry.getKey(), first.getPurchasedAt(),
                    first.getPartnerEmail(), first.getEmailSent(), entry.getValue()));
        }
        return result;
    }

    private FridgeShoppingHistoryVO toVO(String batchId, OffsetDateTime purchasedAt,
                                         String partnerEmail, Boolean emailSent,
                                         List<FridgeShoppingHistory> rows) {
        FridgeShoppingHistoryVO vo = new FridgeShoppingHistoryVO();
        vo.setBatchId(batchId);
        vo.setPurchasedAt(purchasedAt);
        vo.setPartnerEmail(partnerEmail);
        vo.setEmailSent(emailSent);
        List<FridgeShoppingHistoryVO.Item> items = new ArrayList<>();
        for (FridgeShoppingHistory row : rows) {
            FridgeShoppingHistoryVO.Item it = new FridgeShoppingHistoryVO.Item();
            it.setId(row.getId());
            it.setName(row.getName());
            it.setCategoryId(row.getCategoryId());
            it.setQuantity(row.getQuantity());
            it.setUnit(row.getUnit());
            it.setSource(row.getSource());
            items.add(it);
        }
        vo.setItems(items);
        return vo;
    }
}
