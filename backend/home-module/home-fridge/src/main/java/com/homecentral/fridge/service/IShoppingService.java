package com.homecentral.fridge.service;

import com.homecentral.fridge.api.dto.FridgeShoppingConfirmRequest;
import com.homecentral.fridge.api.dto.FridgeShoppingItem;
import com.homecentral.fridge.api.vo.FridgeShoppingHistoryVO;

import java.util.List;

public interface IShoppingService {

    /**
     * 确认购买：把当前采购篮内容写入历史（含 batchId）。
     * 若 partnerEmail 非空且邮件启用，异步发邮件给 partner（不阻塞事务）。
     */
    FridgeShoppingHistoryVO confirm(FridgeShoppingConfirmRequest request, Long userId);

    /**
     * 列出当前用户采购历史（按批次倒序）。
     */
    List<FridgeShoppingHistoryVO> listHistory(Long userId, int limit);

    /**
     * 查询单个批次的详情。
     */
    FridgeShoppingHistoryVO getBatch(String batchId, Long userId);

    /**
     * 复用整个批次：返回条目列表，调用方负责调 quickCreate 入购物篮。
     */
    List<FridgeShoppingItem> reuseBatch(String batchId, Long userId);

    /**
     * 复用单条：返回单个条目，调用方负责调 quickCreate 入采购篮。
     */
    FridgeShoppingItem reuseOne(Long historyId, Long userId);
}
