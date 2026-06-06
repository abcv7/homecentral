package com.homecentral.fridge.controller;

import com.homecentral.common.model.Result;
import com.homecentral.fridge.api.dto.FridgeCategoryRequest;
import com.homecentral.fridge.api.vo.FridgeCategoryVO;
import com.homecentral.fridge.service.IFridgeCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "食材分类", description = "冰箱食材分类管理（系统预置 + 用户自定义）")
@RestController
@RequestMapping("/api/fridge/categories")
public class FridgeCategoryController {

    private final IFridgeCategoryService categoryService;

    public FridgeCategoryController(IFridgeCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "列出可见分类", description = "返回系统预置 + 当前用户自定义的所有分类")
    @GetMapping
    public Result<List<FridgeCategoryVO>> list(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(categoryService.listVisible(userId));
    }

    @Operation(summary = "创建自定义分类")
    @PostMapping
    public Result<FridgeCategoryVO> create(@Valid @RequestBody FridgeCategoryRequest request,
                                           @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(categoryService.create(request, userId));
    }

    @Operation(summary = "更新自定义分类（仅限本人创建）")
    @PutMapping("/{id}")
    public Result<FridgeCategoryVO> update(@PathVariable Long id,
                                           @Valid @RequestBody FridgeCategoryRequest request,
                                           @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(categoryService.update(id, request, userId));
    }

    @Operation(summary = "删除自定义分类（仅限本人创建）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                               @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        categoryService.delete(id, userId);
        return Result.ok(null);
    }
}
