package com.homecentral.fridge.controller;

import com.homecentral.common.model.Result;
import com.homecentral.fridge.api.dto.FridgeItemMoveRequest;
import com.homecentral.fridge.api.dto.FridgeItemRequest;
import com.homecentral.fridge.api.dto.FridgeMigrateRequest;
import com.homecentral.fridge.api.dto.FridgeQuickCreateRequest;
import com.homecentral.fridge.api.enums.FridgeItemStatus;
import com.homecentral.fridge.api.enums.FridgeZone;
import com.homecentral.fridge.api.vo.FridgeExpiringVO;
import com.homecentral.fridge.api.vo.FridgeItemVO;
import com.homecentral.fridge.api.vo.FridgeMigrateResultVO;
import com.homecentral.fridge.service.IFridgeItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "食材管理", description = "冰箱食材 CRUD、批量入库、消耗标记、临期统计、拖拽移动")
@RestController
@RequestMapping("/api/fridge/items")
public class FridgeItemController {

    private final IFridgeItemService itemService;

    public FridgeItemController(IFridgeItemService itemService) {
        this.itemService = itemService;
    }

    @Operation(summary = "新增食材（完整字段）")
    @PostMapping
    public Result<FridgeItemVO> create(@Valid @RequestBody FridgeItemRequest request,
                                       @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(itemService.create(request, userId));
    }

    @Operation(summary = "快速新增（采购篮专用，精简字段，默认数量=1）",
            description = "若 zone/subZone 都为 null 则创建为 PENDING 状态进入采购篮；否则直接入库为 ACTIVE")
    @PostMapping("/quick")
    public Result<FridgeItemVO> quickCreate(@Valid @RequestBody FridgeQuickCreateRequest request,
                                            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(itemService.quickCreate(request, userId));
    }

    @Operation(summary = "拖拽移动食材到目标位置（或退回采购篮）",
            description = "zone+subZone 都为 null 时退回 PENDING；否则写入目标位置，状态从 PENDING 自动转 ACTIVE")
    @PostMapping("/{id}/move")
    public Result<FridgeItemVO> move(@PathVariable Long id,
                                     @RequestBody(required = false) FridgeItemMoveRequest request,
                                     @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(itemService.move(id, request, userId));
    }

    @Operation(summary = "批量新增（拍照识别后确认入库）")
    @PostMapping("/batch")
    public Result<List<FridgeItemVO>> batchCreate(@RequestBody List<FridgeItemRequest> requests,
                                                  @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(itemService.batchCreate(requests, userId));
    }

    @Operation(summary = "列出食材", description = "按区域/分类/状态筛选；includePending=true 时同时返回 PENDING（采购篮）")
    @GetMapping
    public Result<List<FridgeItemVO>> list(@RequestParam(required = false) FridgeZone zone,
                                          @RequestParam(required = false) Long categoryId,
                                          @RequestParam(required = false) FridgeItemStatus status,
                                          @RequestParam(defaultValue = "false") boolean includePending,
                                          @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(itemService.list(userId, zone, categoryId, status, includePending));
    }

    @Operation(summary = "查询单个食材")
    @GetMapping("/{id}")
    public Result<FridgeItemVO> getById(@PathVariable Long id,
                                        @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(itemService.getById(id, userId));
    }

    @Operation(summary = "更新食材")
    @PutMapping("/{id}")
    public Result<FridgeItemVO> update(@PathVariable Long id,
                                       @Valid @RequestBody FridgeItemRequest request,
                                       @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(itemService.update(id, request, userId));
    }

    @Operation(summary = "删除食材（仅 ACTIVE 状态）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                               @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        itemService.delete(id, userId);
        return Result.ok(null);
    }

    @Operation(summary = "标记已消耗（用于吃完/用完后归档）")
    @PostMapping("/{id}/consume")
    public Result<FridgeItemVO> consume(@PathVariable Long id,
                                        @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(itemService.consume(id, userId));
    }

    @Operation(summary = "消耗 1 份（多卡片场景）",
            description = "数量>1 时仅减 1，状态保持 ACTIVE；数量=1 时置为 CONSUMED")
    @PostMapping("/{id}/consume-one")
    public Result<FridgeItemVO> consumeOne(@PathVariable Long id,
                                           @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(itemService.consumeOne(id, userId));
    }

    @Operation(summary = "标记已丢弃（过期/变质/不再食用）")
    @PostMapping("/{id}/discard")
    public Result<FridgeItemVO> discard(@PathVariable Long id,
                                        @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(itemService.discard(id, userId));
    }

    @Operation(summary = "临期/过期统计", description = "返回已过期数 + 距今 N 天内临期数")
    @GetMapping("/expiring")
    public Result<FridgeExpiringVO> expiring(@RequestParam(defaultValue = "3") int days,
                                             @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(itemService.expiringStats(userId, days));
    }

    @Operation(summary = "模板/方案变更时批量迁移",
            description = "把新方案下不兼容的食材（如超层/已删除温区/已删除门）回退到采购篮（PENDING）。仅 ACTIVE 状态参与。")
    @PostMapping("/migrate")
    public Result<FridgeMigrateResultVO> migrate(@Valid @RequestBody FridgeMigrateRequest request,
                                                 @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(itemService.migrateOnLayoutChange(request, userId));
    }
}
