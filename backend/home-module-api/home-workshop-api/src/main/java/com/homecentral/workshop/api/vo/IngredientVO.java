package com.homecentral.workshop.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "原料 VO (鸡尾酒配料 / 酒柜原料共用)")
public class IngredientVO {

    private Long id;
    private String nameZh;
    private String nameEn;
    private java.util.List<String> aliases;
    private BigDecimal defaultBottleMl;
    private Integer cocktailCount;
    private BigDecimal plannedAmountMl;
    private Boolean isMain;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNameZh() { return nameZh; }
    public void setNameZh(String nameZh) { this.nameZh = nameZh; }
    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }
    public java.util.List<String> getAliases() { return aliases; }
    public void setAliases(java.util.List<String> aliases) { this.aliases = aliases; }
    public BigDecimal getDefaultBottleMl() { return defaultBottleMl; }
    public void setDefaultBottleMl(BigDecimal defaultBottleMl) { this.defaultBottleMl = defaultBottleMl; }
    public Integer getCocktailCount() { return cocktailCount; }
    public void setCocktailCount(Integer cocktailCount) { this.cocktailCount = cocktailCount; }
    public BigDecimal getPlannedAmountMl() { return plannedAmountMl; }
    public void setPlannedAmountMl(BigDecimal plannedAmountMl) { this.plannedAmountMl = plannedAmountMl; }
    public Boolean getIsMain() { return isMain; }
    public void setIsMain(Boolean isMain) { this.isMain = isMain; }
}
