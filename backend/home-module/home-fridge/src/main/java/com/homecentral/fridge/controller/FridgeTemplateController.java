package com.homecentral.fridge.controller;

import com.homecentral.common.model.Result;
import com.homecentral.fridge.api.dto.FridgeTemplateRequest;
import com.homecentral.fridge.api.vo.FridgeTemplateVO;
import com.homecentral.fridge.service.IFridgeTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "冰箱模板", description = "冰箱门型/层数模板的持久化与激活")
@RestController
@RequestMapping("/api/fridge/templates")
public class FridgeTemplateController {

    private final IFridgeTemplateService templateService;

    public FridgeTemplateController(IFridgeTemplateService templateService) {
        this.templateService = templateService;
    }

    @Operation(summary = "列出可见模板", description = "返回 4 个系统预置 + 当前用户自定义模板")
    @GetMapping
    public Result<List<FridgeTemplateVO>> list(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(templateService.listVisible(userId));
    }

    @Operation(summary = "获取当前默认模板", description = "若用户尚未激活，返回第一个系统预置模板")
    @GetMapping("/default")
    public Result<FridgeTemplateVO> getDefault(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(templateService.getDefault(userId));
    }

    @Operation(summary = "创建自定义模板")
    @PostMapping
    public Result<FridgeTemplateVO> create(@Valid @RequestBody FridgeTemplateRequest request,
                                           @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(templateService.create(request, userId));
    }

    @Operation(summary = "更新自定义模板（系统预置不可改）")
    @PutMapping("/{id}")
    public Result<FridgeTemplateVO> update(@PathVariable Long id,
                                           @Valid @RequestBody FridgeTemplateRequest request,
                                           @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(templateService.update(id, request, userId));
    }

    @Operation(summary = "删除自定义模板（系统预置 / 当前激活模板不可删）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                               @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        templateService.delete(id, userId);
        return Result.ok(null);
    }

    @Operation(summary = "激活指定模板为默认",
            description = "若激活的是系统预置，会自动克隆一份用户专属副本并激活，避免共享 is_default 冲突")
    @PostMapping("/{id}/activate")
    public Result<FridgeTemplateVO> activate(@PathVariable Long id,
                                             @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(templateService.activate(id, userId));
    }
}
