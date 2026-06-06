package com.homecentral.friend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import com.homecentral.friend.api.enums.RelationshipStatus;

import java.time.OffsetDateTime;

@TableName(value = "friend_relationship", schema = "home_friend")
public class FriendRelationship {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long ownerId;
    private Long friendUserId;
    private Long groupId;
    private RelationshipStatus status;
    private Boolean inviteEmailSent;
    private OffsetDateTime inviteEmailSentAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime respondedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public Long getFriendUserId() { return friendUserId; }
    public void setFriendUserId(Long friendUserId) { this.friendUserId = friendUserId; }
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public RelationshipStatus getStatus() { return status; }
    public void setStatus(RelationshipStatus status) { this.status = status; }
    public Boolean getInviteEmailSent() { return inviteEmailSent; }
    public void setInviteEmailSent(Boolean inviteEmailSent) { this.inviteEmailSent = inviteEmailSent; }
    public OffsetDateTime getInviteEmailSentAt() { return inviteEmailSentAt; }
    public void setInviteEmailSentAt(OffsetDateTime inviteEmailSentAt) { this.inviteEmailSentAt = inviteEmailSentAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(OffsetDateTime respondedAt) { this.respondedAt = respondedAt; }
}
