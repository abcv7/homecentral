package com.homecentral.workshop.service.impl;

import com.homecentral.workshop.api.vo.IngredientVO;
import com.homecentral.workshop.entity.WorkshopIngredient;
import com.homecentral.workshop.mapper.WorkshopIngredientMapper;
import com.homecentral.workshop.service.IIngredientService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 原料服务实现
 * <p>
 * 端口前端 src/api/workshop.ts 的 loadIngredients() (从 public/data/ingredients_index.json 读)
 * 全部走 DB,前端不再需要该静态 JSON。
 */
@Service
public class IngredientServiceImpl implements IIngredientService {

    private final WorkshopIngredientMapper ingredientMapper;

    public IngredientServiceImpl(WorkshopIngredientMapper ingredientMapper) {
        this.ingredientMapper = ingredientMapper;
    }

    @Override
    public List<IngredientVO> listAll() {
        List<WorkshopIngredient> all = ingredientMapper.selectList(null);
        if (all.isEmpty()) return Collections.emptyList();
        List<IngredientVO> out = new ArrayList<>(all.size());
        for (WorkshopIngredient ing : all) {
            out.add(toVO(ing));
        }
        return out;
    }

    private IngredientVO toVO(WorkshopIngredient ing) {
        IngredientVO vo = new IngredientVO();
        vo.setId(ing.getId());
        vo.setNameZh(ing.getNameZh());
        vo.setNameEn(ing.getNameEn());
        vo.setAliases(ing.getAliases() == null ? Collections.emptyList() : Arrays.asList(ing.getAliases()));
        vo.setDefaultBottleMl(ing.getDefaultBottleMl());
        vo.setCocktailCount(ing.getCocktailCount());
        return vo;
    }
}
