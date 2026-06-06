package com.homecentral.fridge.controller;

import com.homecentral.common.model.Result;
import com.homecentral.fridge.api.dto.FridgeShoppingConfirmRequest;
import com.homecentral.fridge.api.dto.FridgeShoppingItem;
import com.homecentral.fridge.api.vo.FridgeShoppingHistoryVO;
import com.homecentral.fridge.service.IShoppingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "采购管理", description = "确认购买、采购历史、复用")
@RestController
@RequestMapping("/api/fridge/shopping")
public class ShoppingController {

    private final IShoppingService shoppingService;

    public ShoppingController(IShoppingService shoppingService) {
        this.shoppingService = shoppingService;
    }

    @Operation(summary = "确认购买", description = "把采购篮内容写入历史；携 partnerEmail 时触发邮件通知（异步）")
    @PostMapping("/confirm")
    public Result<FridgeShoppingHistoryVO> confirm(@Valid @RequestBody FridgeShoppingConfirmRequest request,
                                                   @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(shoppingService.confirm(request, userId));
    }

    @Operation(summary = "采购历史列表", description = "按批次倒序，返回 limit 条（默认 50，上限 200）")
    @GetMapping("/history")
    public Result<List<FridgeShoppingHistoryVO>> listHistory(@RequestParam(defaultValue = "50") int limit,
                                                             @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(shoppingService.listHistory(userId, limit));
    }

    @Operation(summary = "采购历史详情", description = "查询单个批次的全部条目")
    @GetMapping("/history/{batchId}")
    public Result<FridgeShoppingHistoryVO> getBatch(@PathVariable String batchId,
                                                    @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(shoppingService.getBatch(batchId, userId));
    }

    @Operation(summary = "复用整批", description = "返回批次内所有条目，调用方自行 quickCreate 入采购篮")
    @PostMapping("/history/{batchId}/reuse")
    public Result<List<FridgeShoppingItem>> reuseBatch(@PathVariable String batchId,
                                                        @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(shoppingService.reuseBatch(batchId, userId));
    }

    @Operation(summary = "复用单条", description = "返回单条历史条目，调用方自行 quickCreate 入采购篮")
    @PostMapping("/history/item/{historyId}/reuse")
    public Result<FridgeShoppingItem> reuseOne(@PathVariable Long historyId,
                                                @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(shoppingService.reuseOne(historyId, userId));
    }
}
