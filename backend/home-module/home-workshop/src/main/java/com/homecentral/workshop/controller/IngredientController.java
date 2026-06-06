package com.homecentral.workshop.controller;

import com.homecentral.common.model.Result;
import com.homecentral.workshop.api.vo.IngredientVO;
import com.homecentral.workshop.service.IIngredientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "调酒台原料", description = "原料字典查询 (替代前端静态 JSON)")
@RestController
@RequestMapping("/api/workshop/ingredients")
public class IngredientController {

    private final IIngredientService ingredientService;

    public IngredientController(IIngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @Operation(summary = "拉所有原料 (供搜索框 ~1500 条)")
    @GetMapping
    public Result<List<IngredientVO>> listAll() {
        return Result.ok(ingredientService.listAll());
    }
}
