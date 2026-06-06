package com.homecentral.workshop.service.impl;

import com.homecentral.workshop.api.dto.RecommendRequest;
import com.homecentral.workshop.api.vo.RecommendResultVO;
import com.homecentral.workshop.entity.WorkshopCocktail;
import com.homecentral.workshop.entity.WorkshopCocktailIngredient;
import com.homecentral.workshop.entity.WorkshopIngredient;
import com.homecentral.workshop.mapper.WorkshopCocktailIngredientMapper;
import com.homecentral.workshop.mapper.WorkshopCocktailMapper;
import com.homecentral.workshop.mapper.WorkshopIngredientMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

/**
 * 镜像 src/main/resources/frontend WebPage/src/utils/workshop-recommend.test.ts 的 7 个 case,
 * 保证 FE→BE 行为完全等价。
 */
@ExtendWith(MockitoExtension.class)
class RecommendServiceImplTest {

    @Mock
    private WorkshopCocktailMapper cocktailMapper;

    @Mock
    private WorkshopCocktailIngredientMapper cocktailIngredientMapper;

    @Mock
    private WorkshopIngredientMapper ingredientMapper;

    @InjectMocks
    private RecommendServiceImpl service;

    /** Cocktail 实体构建: 显式属性必须在 ...over 之前, 避免 TS2783 / Java 重复 setter */
    private WorkshopCocktail cocktail(long id, String name, int views) {
        WorkshopCocktail c = new WorkshopCocktail();
        c.setId(id);
        c.setNameZh(name);
        c.setNameEn("");
        c.setViews(views);
        return c;
    }

    /** 关联行: cocktailId, ingredientId, isMain */
    private WorkshopCocktailIngredient link(long cocktailId, long ingredientId, boolean isMain) {
        WorkshopCocktailIngredient l = new WorkshopCocktailIngredient();
        l.setCocktailId(cocktailId);
        l.setIngredientId(ingredientId);
        l.setIsMain(isMain);
        return l;
    }

    private WorkshopIngredient ingredient(long id, String name) {
        WorkshopIngredient i = new WorkshopIngredient();
        i.setId(id);
        i.setNameZh(name);
        i.setNameEn("");
        return i;
    }

    private void stub(List<WorkshopCocktail> cocktails, List<WorkshopCocktailIngredient> links, List<WorkshopIngredient> ings) {
        when(cocktailMapper.selectList(isNull())).thenReturn(cocktails);
        when(cocktailIngredientMapper.selectList(isNull())).thenReturn(links);
        when(ingredientMapper.selectList(isNull())).thenReturn(ings);
    }

    @Test
    void strict_mode_requires_all_ingredients() {
        WorkshopCocktail ginTonic = cocktail(1, "Gin Tonic", 0);
        WorkshopCocktail negroni = cocktail(2, "Negroni", 0);
        stub(
            List.of(ginTonic, negroni),
            List.of(
                link(1, 10, true), link(1, 20, true),
                link(2, 10, true), link(2, 20, true), link(2, 30, true)
            ),
            List.of(ingredient(10, "Gin"), ingredient(20, "Tonic"), ingredient(30, "Campari"))
        );

        RecommendRequest req = new RecommendRequest();
        req.setIngredientIds(List.of(10L, 20L));
        req.setMode("STRICT");

        List<RecommendResultVO> r = service.recommend(req);

        assertEquals(2, r.size());
        assertEquals(1L, r.get(0).getCocktail().getId());
        assertEquals("full", r.get(0).getTier());
        assertEquals(0, r.get(0).getMissingCount().intValue());
        assertEquals(2L, r.get(1).getCocktail().getId());
        assertEquals("miss-1", r.get(1).getTier());
        assertEquals(1, r.get(1).getMissingIngredients().size());
        assertEquals(30L, r.get(1).getMissingIngredients().get(0).getId());
    }

    @Test
    void main_mode_ignores_non_main_ingredients() {
        WorkshopCocktail martini = cocktail(1, "Martini", 0);
        stub(
            List.of(martini),
            List.of(
                link(1, 10, true), link(1, 20, true),  // 主料
                link(1, 30, false)                       // 装饰
            ),
            List.of(ingredient(10, "Gin"), ingredient(20, "Vermouth"), ingredient(30, "Olive"))
        );

        RecommendRequest req = new RecommendRequest();
        req.setIngredientIds(List.of(10L, 20L));
        req.setMode("MAIN");

        List<RecommendResultVO> r = service.recommend(req);

        assertEquals(1, r.size());
        assertEquals("full", r.get(0).getTier());
        assertEquals(0, r.get(0).getMissingCount().intValue());
    }

    @Test
    void main_mode_skips_cocktails_with_empty_main_ingredient_ids() {
        WorkshopCocktail incomplete = cocktail(1, "Incomplete", 0);
        stub(
            List.of(incomplete),
            List.of(link(1, 10, false), link(1, 20, false)),
            List.of(ingredient(10, "X"), ingredient(20, "Y"))
        );

        RecommendRequest req = new RecommendRequest();
        req.setIngredientIds(List.of(10L, 20L));
        req.setMode("MAIN");

        List<RecommendResultVO> r = service.recommend(req);
        assertEquals(0, r.size());
    }

    @Test
    void filters_out_zero_match_in_both_modes() {
        WorkshopCocktail a = cocktail(1, "A", 0);
        WorkshopCocktail b = cocktail(2, "B", 0);
        stub(
            List.of(a, b),
            List.of(
                link(1, 99, true), link(1, 100, true),
                link(2, 10, true), link(2, 20, true)
            ),
            List.of(ingredient(10, "X"), ingredient(20, "Y"), ingredient(99, "Z"), ingredient(100, "W"))
        );

        RecommendRequest req = new RecommendRequest();
        req.setIngredientIds(List.of(10L, 20L));
        req.setMode("STRICT");

        List<RecommendResultVO> r = service.recommend(req);
        assertEquals(1, r.size());
        assertEquals(2L, r.get(0).getCocktail().getId());
    }

    @Test
    void tiers_are_assigned_by_missing_count() {
        WorkshopCocktail a = cocktail(1, "A", 0);
        WorkshopCocktail b = cocktail(2, "B", 0);
        WorkshopCocktail c = cocktail(3, "C", 0);
        List<WorkshopCocktailIngredient> links = new ArrayList<>();
        for (long i : new long[]{1, 2, 3}) links.add(link(1, i, true));
        for (long i : new long[]{1, 2, 3, 4}) links.add(link(2, i, true));
        for (long i : new long[]{1, 2, 3, 4, 5}) links.add(link(3, i, true));
        List<WorkshopIngredient> ings = List.of(
            ingredient(1, "1"), ingredient(2, "2"), ingredient(3, "3"),
            ingredient(4, "4"), ingredient(5, "5")
        );
        stub(List.of(a, b, c), links, ings);

        RecommendRequest req = new RecommendRequest();
        req.setIngredientIds(List.of(1L));
        req.setMode("STRICT");

        List<RecommendResultVO> r = service.recommend(req);
        List<String> tiers = r.stream().map(RecommendResultVO::getTier).sorted().toList();
        assertEquals(List.of("miss-2", "miss-3+", "miss-3+"), tiers);
    }

    @Test
    void sort_tier_first_then_ratio_desc_then_views_desc() {
        WorkshopCocktail fullLowViews = cocktail(1, "LowViews", 1);
        WorkshopCocktail fullHighViews = cocktail(2, "HighViews", 999);
        WorkshopCocktail miss1 = cocktail(3, "Miss1", 99999);
        stub(
            List.of(fullLowViews, fullHighViews, miss1),
            List.of(
                link(1, 10, true), link(1, 20, true),
                link(2, 10, true), link(2, 20, true),
                link(3, 10, true), link(3, 30, true)
            ),
            List.of(ingredient(10, "Gin"), ingredient(20, "Tonic"), ingredient(30, "Lime"))
        );

        RecommendRequest req = new RecommendRequest();
        req.setIngredientIds(List.of(10L, 20L));
        req.setMode("STRICT");

        List<RecommendResultVO> r = service.recommend(req);
        assertEquals(3, r.size());
        assertEquals("full", r.get(0).getTier());
        assertEquals(2L, r.get(0).getCocktail().getId());
        assertEquals("full", r.get(1).getTier());
        assertEquals(1L, r.get(1).getCocktail().getId());
        assertEquals("miss-1", r.get(2).getTier());
    }

    @Test
    void null_or_empty_ingredient_ids_returns_empty() {
        RecommendRequest req = new RecommendRequest();
        req.setIngredientIds(null);
        req.setMode("STRICT");
        assertEquals(0, service.recommend(req).size());

        req.setIngredientIds(Collections.emptyList());
        assertEquals(0, service.recommend(req).size());
    }

    @Test
    void empty_cocktail_db_returns_empty() {
        when(cocktailMapper.selectList(isNull())).thenReturn(Collections.emptyList());

        RecommendRequest req = new RecommendRequest();
        req.setIngredientIds(List.of(10L));
        req.setMode("STRICT");

        assertEquals(0, service.recommend(req).size());
    }
}
