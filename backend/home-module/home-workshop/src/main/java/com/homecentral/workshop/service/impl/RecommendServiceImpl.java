package com.homecentral.workshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homecentral.workshop.api.dto.RecommendRequest;
import com.homecentral.workshop.api.vo.CocktailVO;
import com.homecentral.workshop.api.vo.IngredientVO;
import com.homecentral.workshop.api.vo.RecommendResultVO;
import com.homecentral.workshop.entity.WorkshopCocktail;
import com.homecentral.workshop.entity.WorkshopCocktailIngredient;
import com.homecentral.workshop.entity.WorkshopIngredient;
import com.homecentral.workshop.mapper.WorkshopCocktailIngredientMapper;
import com.homecentral.workshop.mapper.WorkshopCocktailMapper;
import com.homecentral.workshop.mapper.WorkshopIngredientMapper;
import com.homecentral.workshop.service.IRecommendService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 调酒台推荐服务实现
 * <p>
 * 算法端口自 frontend WebPage/src/utils/workshop-recommend.ts 的 recommend() 纯函数,
 * 行为完全等价:tier 优先 -> ratio 降序 -> views 降序;0 命中过滤;主料模式空集过滤。
 * <p>
 * 实现:
 * 1. 拉所有 cocktail + cocktail_ingredient + ingredient 三表(数据量小,~600 款酒,~1500 行配料)
 * 2. 在内存里按 cocktail 分组,构建 main/全量 id 集合
 * 3. 对每款酒跑纯函数 recommend() 同样的逻辑
 * 4. 转 VO 返回
 */
@Service
public class RecommendServiceImpl implements IRecommendService {

    private final WorkshopCocktailMapper cocktailMapper;
    private final WorkshopCocktailIngredientMapper cocktailIngredientMapper;
    private final WorkshopIngredientMapper ingredientMapper;

    public RecommendServiceImpl(WorkshopCocktailMapper cocktailMapper,
                                WorkshopCocktailIngredientMapper cocktailIngredientMapper,
                                WorkshopIngredientMapper ingredientMapper) {
        this.cocktailMapper = cocktailMapper;
        this.cocktailIngredientMapper = cocktailIngredientMapper;
        this.ingredientMapper = ingredientMapper;
    }

    @Override
    public List<RecommendResultVO> recommend(RecommendRequest request) {
        if (request == null || request.getIngredientIds() == null || request.getIngredientIds().isEmpty()) {
            return Collections.emptyList();
        }
        String mode = request.getMode() == null ? "MAIN" : request.getMode().toUpperCase();
        Set<Long> owned = request.getIngredientIds().stream().collect(Collectors.toSet());

        List<WorkshopCocktail> cocktails = cocktailMapper.selectList(null);
        if (cocktails.isEmpty()) {
            return Collections.emptyList();
        }

        // 一次性拉所有配料关联 + 原料
        List<WorkshopCocktailIngredient> allLinks = cocktailIngredientMapper.selectList(null);
        Map<Long, Map<Long, WorkshopCocktailIngredient>> linksByCocktail = new HashMap<>();
        for (WorkshopCocktailIngredient link : allLinks) {
            linksByCocktail
                    .computeIfAbsent(link.getCocktailId(), k -> new HashMap<>())
                    .put(link.getIngredientId(), link);
        }
        List<WorkshopIngredient> ingredients = ingredientMapper.selectList(null);
        Map<Long, WorkshopIngredient> ingredientById = ingredients.stream()
                .collect(Collectors.toMap(WorkshopIngredient::getId, i -> i));

        List<RecommendResultVO> out = new ArrayList<>();
        for (WorkshopCocktail cocktail : cocktails) {
            Map<Long, WorkshopCocktailIngredient> links = linksByCocktail.getOrDefault(cocktail.getId(), Collections.emptyMap());

            List<Long> required = new ArrayList<>();
            List<Long> mainRequired = new ArrayList<>();
            for (Map.Entry<Long, WorkshopCocktailIngredient> e : links.entrySet()) {
                required.add(e.getKey());
                if (Boolean.TRUE.equals(e.getValue().getIsMain())) {
                    mainRequired.add(e.getKey());
                }
            }

            List<Long> requiredIds = "STRICT".equals(mode) ? required : mainRequired;

            // 主料模式下,该酒无 main 字段 -> 残缺数据,跳过
            if ("MAIN".equals(mode) && requiredIds.isEmpty()) continue;

            List<Long> matched = new ArrayList<>();
            List<Long> missing = new ArrayList<>();
            for (Long id : requiredIds) {
                (owned.contains(id) ? matched : missing).add(id);
            }

            // 0 命中过滤(两种模式都过滤,避免空结果噪声)
            if (matched.isEmpty()) continue;

            int missingCount = missing.size();
            String tier = missingCount == 0 ? "full"
                    : missingCount == 1 ? "miss-1"
                    : missingCount == 2 ? "miss-2"
                    : "miss-3+";

            RecommendResultVO vo = new RecommendResultVO();
            vo.setTier(tier);
            vo.setCocktail(toCocktailVO(cocktail, links, ingredientById));
            vo.setMissingIngredients(toIngredientVOs(missing, links, ingredientById));
            vo.setMatchedIngredients(toIngredientVOs(matched, links, ingredientById));
            vo.setMissingCount(BigDecimal.valueOf(missingCount));
            out.add(vo);
        }

        // 排序:TIER_ORDER 优先 -> ratio 降序 -> views 降序
        final Map<String, Integer> tierOrder = Map.of("full", 0, "miss-1", 1, "miss-2", 2, "miss-3+", 3);
        out.sort((a, b) -> {
            int tierCmp = Integer.compare(tierOrder.get(a.getTier()), tierOrder.get(b.getTier()));
            if (tierCmp != 0) return tierCmp;
            int aMatched = a.getMatchedIngredients() == null ? 0 : a.getMatchedIngredients().size();
            int bMatched = b.getMatchedIngredients() == null ? 0 : b.getMatchedIngredients().size();
            int aRequired = aMatched + (a.getMissingCount() == null ? 0 : a.getMissingCount().intValue());
            int bRequired = bMatched + (b.getMissingCount() == null ? 0 : b.getMissingCount().intValue());
            double aRatio = aRequired > 0 ? (double) aMatched / aRequired : 0d;
            double bRatio = bRequired > 0 ? (double) bMatched / bRequired : 0d;
            int ratioCmp = Double.compare(bRatio, aRatio);
            if (ratioCmp != 0) return ratioCmp;
            int aViews = a.getCocktail() == null || a.getCocktail().getViews() == null ? 0 : a.getCocktail().getViews();
            int bViews = b.getCocktail() == null || b.getCocktail().getViews() == null ? 0 : b.getCocktail().getViews();
            return Integer.compare(bViews, aViews);
        });

        return out;
    }

    private CocktailVO toCocktailVO(WorkshopCocktail c,
                                    Map<Long, WorkshopCocktailIngredient> links,
                                    Map<Long, WorkshopIngredient> ingredientById) {
        CocktailVO vo = new CocktailVO();
        vo.setId(c.getId());
        vo.setNameZh(c.getNameZh());
        vo.setNameEn(c.getNameEn());
        vo.setNameAlias(c.getNameAlias() == null ? Collections.emptyList() : Arrays.asList(c.getNameAlias()));
        vo.setCover(c.getCover());
        vo.setViews(c.getViews());
        vo.setRecipeZh(c.getRecipeZh());
        vo.setMethodZh(c.getMethodZh());
        vo.setAroma(c.getAroma());
        vo.setTaste(c.getTaste());
        vo.setStyle(c.getStyle());
        vo.setScene(c.getScene());
        vo.setHistory(c.getHistory());
        vo.setSourceUrl(c.getSourceUrl());
        vo.setLastSyncedAt(c.getLastSyncedAt());
        // 配料列表(只放 main 在前,非 main 在后)
        List<IngredientVO> ingredients = new ArrayList<>();
        links.entrySet().stream()
                .sorted((a, b) -> {
                    boolean aMain = Boolean.TRUE.equals(a.getValue().getIsMain());
                    boolean bMain = Boolean.TRUE.equals(b.getValue().getIsMain());
                    if (aMain != bMain) return aMain ? -1 : 1;
                    Integer ao = a.getValue().getSortOrder();
                    Integer bo = b.getValue().getSortOrder();
                    if (ao == null && bo == null) return 0;
                    if (ao == null) return 1;
                    if (bo == null) return -1;
                    return Integer.compare(ao, bo);
                })
                .forEach(e -> {
                    WorkshopIngredient ing = ingredientById.get(e.getKey());
                    if (ing != null) {
                        ingredients.add(toIngredientVO(ing, e.getValue()));
                    }
                });
        vo.setIngredients(ingredients);
        return vo;
    }

    private List<IngredientVO> toIngredientVOs(List<Long> ids,
                                                Map<Long, WorkshopCocktailIngredient> links,
                                                Map<Long, WorkshopIngredient> ingredientById) {
        List<IngredientVO> out = new ArrayList<>();
        for (Long id : ids) {
            WorkshopIngredient ing = ingredientById.get(id);
            if (ing == null) continue;
            WorkshopCocktailIngredient link = links.get(id);
            out.add(toIngredientVO(ing, link));
        }
        return out;
    }

    private IngredientVO toIngredientVO(WorkshopIngredient ing, WorkshopCocktailIngredient link) {
        IngredientVO vo = new IngredientVO();
        vo.setId(ing.getId());
        vo.setNameZh(ing.getNameZh());
        vo.setNameEn(ing.getNameEn());
        vo.setAliases(ing.getAliases() == null ? Collections.emptyList() : Arrays.asList(ing.getAliases()));
        vo.setDefaultBottleMl(ing.getDefaultBottleMl());
        vo.setCocktailCount(ing.getCocktailCount());
        if (link != null) {
            vo.setPlannedAmountMl(link.getPlannedAmountMl());
            vo.setIsMain(link.getIsMain());
        }
        return vo;
    }
}
