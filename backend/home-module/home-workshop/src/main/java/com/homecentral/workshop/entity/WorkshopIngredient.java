package com.homecentral.workshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@TableName("workshop_ingredient")
public class WorkshopIngredient {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String nameZh;
    private String nameEn;

    @TableField(typeHandler = com.homecentral.workshop.config.StringArrayTypeHandler.class)
    private String[] aliases;

    private BigDecimal defaultBottleMl;
    private Integer cocktailCount;
    private OffsetDateTime lastSyncedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNameZh() { return nameZh; }
    public void setNameZh(String nameZh) { this.nameZh = nameZh; }
    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }
    public String[] getAliases() { return aliases; }
    public void setAliases(String[] aliases) { this.aliases = aliases; }
    public BigDecimal getDefaultBottleMl() { return defaultBottleMl; }
    public void setDefaultBottleMl(BigDecimal defaultBottleMl) { this.defaultBottleMl = defaultBottleMl; }
    public Integer getCocktailCount() { return cocktailCount; }
    public void setCocktailCount(Integer cocktailCount) { this.cocktailCount = cocktailCount; }
    public OffsetDateTime getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(OffsetDateTime lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
