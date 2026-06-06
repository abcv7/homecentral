package com.homecentral.friend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import com.homecentral.friend.api.enums.GroupType;

import java.time.OffsetDateTime;

@TableName(value = "friend_group", schema = "home_friend")
public class FriendGroup {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long ownerId;
    private String name;
    private GroupType type;
    private String color;
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public GroupType getType() { return type; }
    public void setType(GroupType type) { this.type = type; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
