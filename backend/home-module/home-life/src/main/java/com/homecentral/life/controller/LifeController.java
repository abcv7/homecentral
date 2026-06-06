package com.homecentral.life.controller;

import com.homecentral.common.model.Result;
import com.homecentral.life.api.dto.AnniversaryCreateRequest;
import com.homecentral.life.api.dto.ReminderRuleCreateRequest;
import com.homecentral.life.api.dto.ShoppingMemoCreateRequest;
import com.homecentral.life.api.vo.AnniversaryVO;
import com.homecentral.life.api.vo.ReminderRuleVO;
import com.homecentral.life.api.vo.ShoppingMemoVO;
import com.homecentral.life.service.ILifeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "生活服务", description = "购物清单、纪念日、提醒规则管理")
@RestController
@RequestMapping("/api/life")
public class LifeController {

    private final ILifeService lifeService;

    public LifeController(ILifeService lifeService) {
        this.lifeService = lifeService;
    }

    @Operation(summary = "创建购物清单", description = "添加新的购物清单项")
    @PostMapping("/shopping-memos")
    public Result<ShoppingMemoVO> createShoppingMemo(@Valid @RequestBody ShoppingMemoCreateRequest request,
                                                     @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(lifeService.createShoppingMemo(request, userId));
    }

    @Operation(summary = "购物清单列表", description = "获取当前用户的购物清单列表")
    @GetMapping("/shopping-memos")
    public Result<List<ShoppingMemoVO>> listShoppingMemos(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(lifeService.listShoppingMemos(userId));
    }

    @Operation(summary = "切换已购状态", description = "切换购物清单项的已购买状态")
    @PutMapping("/shopping-memos/{id}/toggle")
    public Result<ShoppingMemoVO> togglePurchased(@PathVariable Long id,
                                                  @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(lifeService.togglePurchased(id, userId));
    }

    @Operation(summary = "创建纪念日", description = "添加新的纪念日")
    @PostMapping("/anniversaries")
    public Result<AnniversaryVO> createAnniversary(@Valid @RequestBody AnniversaryCreateRequest request,
                                                   @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(lifeService.createAnniversary(request, userId));
    }

    @Operation(summary = "纪念日列表", description = "获取当前用户的纪念日列表")
    @GetMapping("/anniversaries")
    public Result<List<AnniversaryVO>> listAnniversaries(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(lifeService.listAnniversaries(userId));
    }

    @Operation(summary = "创建提醒规则", description = "添加新的提醒规则")
    @PostMapping("/reminder-rules")
    public Result<ReminderRuleVO> createReminderRule(@Valid @RequestBody ReminderRuleCreateRequest request,
                                                     @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(lifeService.createReminderRule(request, userId));
    }

    @Operation(summary = "提醒规则列表", description = "获取当前用户的提醒规则列表")
    @GetMapping("/reminder-rules")
    public Result<List<ReminderRuleVO>> listReminderRules(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(lifeService.listReminderRules(userId));
    }

    @Operation(summary = "切换启用状态", description = "切换提醒规则的启用/禁用状态")
    @PutMapping("/reminder-rules/{id}/toggle")
    public Result<ReminderRuleVO> toggleEnabled(@PathVariable Long id,
                                                @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(lifeService.toggleEnabled(id, userId));
    }
}
