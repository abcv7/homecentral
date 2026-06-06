package com.homecentral.fridge.service;

import com.homecentral.fridge.api.dto.FridgeItemMoveRequest;
import com.homecentral.fridge.api.dto.FridgeItemRequest;
import com.homecentral.fridge.api.dto.FridgeMigrateRequest;
import com.homecentral.fridge.api.dto.FridgeQuickCreateRequest;
import com.homecentral.fridge.api.enums.FridgeItemStatus;
import com.homecentral.fridge.api.enums.FridgeZone;
import com.homecentral.fridge.api.vo.FridgeExpiringVO;
import com.homecentral.fridge.api.vo.FridgeItemVO;
import com.homecentral.fridge.api.vo.FridgeMigrateResultVO;

import java.util.List;

public interface IFridgeItemService {

    FridgeItemVO create(FridgeItemRequest request, Long userId);

    FridgeItemVO update(Long id, FridgeItemRequest request, Long userId);

    FridgeItemVO getById(Long id, Long userId);

    void delete(Long id, Long userId);

    FridgeItemVO consume(Long id, Long userId);

    /**
     * 消耗 1 份（多卡片场景）：
     * <ul>
     *     <li>数量 &gt; 1：数量 -1，状态保持 ACTIVE</li>
     *     <li>数量 = 1：状态置为 CONSUMED（数量保持 1 以便历史查询）</li>
     * </ul>
     */
    FridgeItemVO consumeOne(Long id, Long userId);

    FridgeItemVO discard(Long id, Long userId);

    /**
     * 列出当前用户食材，支持按区域/分类/状态筛选。
     * 排序：临期优先（过期最早）→ 未设过期 → 最近创建。
     *
     * @param includePending 若为 true，同时返回 PENDING（采购篮）状态食材；默认 false（仅 ACTIVE）
     */
    List<FridgeItemVO> list(Long userId, FridgeZone zone, Long categoryId, FridgeItemStatus status, boolean includePending);

    /**
     * 批量创建（拍照识别后确认入库）。
     * 全部成功提交；任一失败回滚整批。
     */
    List<FridgeItemVO> batchCreate(List<FridgeItemRequest> requests, Long userId);

    /**
     * 临期/过期统计。
     *
     * @param days 临期阈值（默认 3）
     */
    FridgeExpiringVO expiringStats(Long userId, int days);

    /**
     * 拖拽移动食材到目标位置。
     * <ul>
     *     <li>zone = null 且 subZone = null：退回采购篮（PENDING）</li>
     *     <li>否则：写入新 zone + subZone，状态从 PENDING 自动转 ACTIVE</li>
     * </ul>
     */
    FridgeItemVO move(Long id, FridgeItemMoveRequest request, Long userId);

    /**
     * 快速新增（采购篮专用）：默认数量 1，无单位/备注字段。
     * <ul>
     *     <li>若 zone/subZone 都不为 null：直接入库为 ACTIVE</li>
     *     <li>否则：进入采购篮（PENDING），等待用户拖拽归位</li>
     * </ul>
     */
    FridgeItemVO quickCreate(FridgeQuickCreateRequest request, Long userId);

    /**
     * 模板/方案变更时批量迁移：把已 ACTIVE 但在新方案下不再兼容的食材回退到采购篮（PENDING）。
     * <p>
     * 触发场景：
     * <ul>
     *     <li>切换模板（如 THREE_DOOR → CLASSIC 移除 chiller、解冻区食材回采购篮）</li>
     *     <li>更新模板（减小层数 / 门数；超层食材回采购篮）</li>
     *     <li>门型从对开/三门变单门（门搁板食材回采购篮）</li>
     * </ul>
     * <p>
     * 兼容规则：subZone 严格按 {@code (ZONE|DOOR-SIDE)-L<digits>} 编码做层数检查；
     * 中文旧 subZone（"中层"、"门左"等）按所在 zone 是否还存在判断。
     * <p>
     * 仅 ACTIVE 状态食材参与迁移；PENDING / CONSUMED / DISCARDED 不动。
     * 事务：单次迁移在 service 层事务内执行。
     */
    FridgeMigrateResultVO migrateOnLayoutChange(FridgeMigrateRequest request, Long userId);
}
