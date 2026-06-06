package com.homecentral.workshop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.homecentral.workshop.entity.WorkshopCocktail;
import com.homecentral.workshop.entity.WorkshopCocktailIngredient;
import com.homecentral.workshop.entity.WorkshopIngredient;
import com.homecentral.workshop.mapper.WorkshopCocktailIngredientMapper;
import com.homecentral.workshop.mapper.WorkshopCocktailMapper;
import com.homecentral.workshop.mapper.WorkshopIngredientMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Configuration
public class WorkshopDataSeeder {

    private static final Logger log = LoggerFactory.getLogger(WorkshopDataSeeder.class);

    private static final BigDecimal MAIN_PLANNED_ML = new BigDecimal("30.0");
    private static final BigDecimal REGULAR_PLANNED_ML = new BigDecimal("15.0");
    private static final BigDecimal DEFAULT_BOTTLE_ML = new BigDecimal("750.0");

    private static final Map<String, BigDecimal> KNOWN_BOTTLES = new HashMap<>();
    static {
        KNOWN_BOTTLES.put("vodka", new BigDecimal("750.0"));
        KNOWN_BOTTLES.put("gin", new BigDecimal("750.0"));
        KNOWN_BOTTLES.put("rum", new BigDecimal("750.0"));
        KNOWN_BOTTLES.put("tequila", new BigDecimal("750.0"));
        KNOWN_BOTTLES.put("whiskey", new BigDecimal("750.0"));
        KNOWN_BOTTLES.put("whisky", new BigDecimal("750.0"));
        KNOWN_BOTTLES.put("cognac", new BigDecimal("700.0"));
        KNOWN_BOTTLES.put("campari", new BigDecimal("750.0"));
        KNOWN_BOTTLES.put("vermouth", new BigDecimal("750.0"));
        KNOWN_BOTTLES.put("cointreau", new BigDecimal("700.0"));
        KNOWN_BOTTLES.put("triple sec", new BigDecimal("700.0"));
        KNOWN_BOTTLES.put("chartreuse", new BigDecimal("500.0"));
        KNOWN_BOTTLES.put("absinthe", new BigDecimal("700.0"));
    }

    @Bean
    public ApplicationRunner seedWorkshopData(WorkshopCocktailMapper cocktailMapper,
                                              WorkshopIngredientMapper ingredientMapper,
                                              WorkshopCocktailIngredientMapper ciMapper) {
        return args -> seed(cocktailMapper, ingredientMapper, ciMapper);
    }

    @Transactional
    void seed(WorkshopCocktailMapper cocktailMapper,
              WorkshopIngredientMapper ingredientMapper,
              WorkshopCocktailIngredientMapper ciMapper) {
        long existingCocktails = cocktailMapper.selectCount(null);
        if (existingCocktails > 0) {
            log.info("[workshop] data already seeded (cocktails={}), skip", existingCocktails);
            return;
        }
        ObjectMapper json = new ObjectMapper();
        Map<String, WorkshopIngredient> ingredientByName = new HashMap<>();
        try {
            log.info("[workshop] starting first-time data seed from classpath:data/*.json");
            InputStream ingStream = new ClassPathResource("data/ingredients_index.json").getInputStream();
            JsonNode ingRoot = json.readTree(ingStream);
            Iterator<JsonNode> ingIt = ingRoot.elements();
            int ingCount = 0;
            while (ingIt.hasNext()) {
                JsonNode n = ingIt.next();
                WorkshopIngredient ing = new WorkshopIngredient();
                ing.setNameZh(textOrNull(n, "name_zh"));
                ing.setNameEn(textOrNull(n, "name_en"));
                ing.setAliases(arrayOrNull(n, "aliases"));
                ing.setCocktailCount(n.path("cocktail_count").asInt(0));
                ing.setDefaultBottleMl(guessBottleMl(ing));
                ing.setCreatedAt(OffsetDateTime.now());
                ing.setUpdatedAt(OffsetDateTime.now());
                ingredientMapper.insert(ing);
                ingredientByName.put(ing.getNameZh(), ing);
                ingredientByName.put(ing.getNameEn(), ing);
                ingCount++;
            }
            log.info("[workshop] seeded {} ingredients", ingCount);

            Map<Long, WorkshopIngredient> ingredientById = new HashMap<>();
            ingredientMapper.selectList(null).forEach(i -> ingredientById.put(i.getId(), i));

            InputStream cStream = new ClassPathResource("data/cocktails_index.json").getInputStream();
            JsonNode cRoot = json.readTree(cStream);
            Iterator<JsonNode> cIt = cRoot.elements();
            int cCount = 0;
            int relationCount = 0;
            List<WorkshopCocktailIngredient> batch = new ArrayList<>();
            int batchSize = 0;
            while (cIt.hasNext()) {
                JsonNode n = cIt.next();
                WorkshopCocktail c = new WorkshopCocktail();
                c.setNameZh(textOrNull(n, "name_zh"));
                c.setNameEn(textOrNull(n, "name_en"));
                c.setNameAlias(arrayOrNull(n, "name_alias"));
                c.setCover(textOrNull(n, "cover"));
                c.setViews(n.path("views").asInt(0));
                c.setRecipeZh(textOrNull(n, "recipe_zh"));
                c.setMethodZh(textOrNull(n, "method_zh"));
                c.setAroma(textOrNull(n, "aroma"));
                c.setTaste(textOrNull(n, "taste"));
                c.setStyle(textOrNull(n, "style"));
                c.setScene(textOrNull(n, "scene"));
                c.setHistory(textOrNull(n, "history"));
                c.setSourceUrl(textOrNull(n, "source_url"));
                c.setLastSyncedAt(OffsetDateTime.now());
                c.setCreatedAt(OffsetDateTime.now());
                c.setUpdatedAt(OffsetDateTime.now());
                cocktailMapper.insert(c);
                cCount++;

                List<Long> allIngs = longList(n, "ingredient_ids");
                List<Long> mainIngs = longList(n, "main_ingredient_ids");
                for (Long ingId : allIngs) {
                    WorkshopCocktailIngredient ci = new WorkshopCocktailIngredient();
                    ci.setCocktailId(c.getId());
                    ci.setIngredientId(ingId);
                    ci.setIsMain(mainIngs.contains(ingId));
                    ci.setPlannedAmountMl(ci.getIsMain() ? MAIN_PLANNED_ML : REGULAR_PLANNED_ML);
                    ci.setSortOrder(0);
                    batch.add(ci);
                    batchSize++;
                    relationCount++;
                }
                if (batchSize >= 500) {
                    for (WorkshopCocktailIngredient ci : batch) {
                        ciMapper.insert(ci);
                    }
                    batch.clear();
                    batchSize = 0;
                }
            }
            if (!batch.isEmpty()) {
                for (WorkshopCocktailIngredient ci : batch) {
                    ciMapper.insert(ci);
                }
            }
            log.info("[workshop] seeded {} cocktails, {} relations", cCount, relationCount);
        } catch (Exception e) {
            log.error("[workshop] data seed failed", e);
            throw new IllegalStateException("workshop data seed failed", e);
        }
    }

    private BigDecimal guessBottleMl(WorkshopIngredient ing) {
        if (ing.getNameEn() == null) {
            return DEFAULT_BOTTLE_ML;
        }
        String name = ing.getNameEn().toLowerCase();
        for (Map.Entry<String, BigDecimal> e : KNOWN_BOTTLES.entrySet()) {
            if (name.contains(e.getKey())) {
                return e.getValue();
            }
        }
        if (name.contains("syrup") || name.contains("juice") || name.contains("soda")
                || name.contains("water") || name.contains("tonic") || name.contains("lemon")
                || name.contains("lime") || name.contains("mint") || name.contains("bitters")) {
            return null;
        }
        return DEFAULT_BOTTLE_ML;
    }

    private String textOrNull(JsonNode n, String field) {
        JsonNode v = n.get(field);
        if (v == null || v.isNull() || v.asText().isBlank()) {
            return null;
        }
        return v.asText();
    }

    private String[] arrayOrNull(JsonNode n, String field) {
        JsonNode v = n.get(field);
        if (v == null || !v.isArray() || v.isEmpty()) {
            return null;
        }
        List<String> out = new ArrayList<>();
        v.forEach(x -> out.add(x.asText()));
        return out.toArray(new String[0]);
    }

    private List<Long> longList(JsonNode n, String field) {
        JsonNode v = n.get(field);
        List<Long> out = new ArrayList<>();
        if (v == null || !v.isArray()) {
            return out;
        }
        v.forEach(x -> out.add(x.asLong()));
        return out;
    }
}
