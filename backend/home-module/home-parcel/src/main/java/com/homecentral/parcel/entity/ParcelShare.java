package com.homecentral.parcel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.OffsetDateTime;

@TableName(value = "parcel_share", schema = "home_parcel")
public class ParcelShare {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long parcelId;
    private Long sharedWithUserId;
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getParcelId() { return parcelId; }
    public void setParcelId(Long parcelId) { this.parcelId = parcelId; }
    public Long getSharedWithUserId() { return sharedWithUserId; }
    public void setSharedWithUserId(Long sharedWithUserId) { this.sharedWithUserId = sharedWithUserId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
