package com.homecentral.friend.api.vo;

import com.homecentral.friend.api.enums.GroupType;
import com.homecentral.friend.api.enums.RelationshipStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "好友关系响应")
public class FriendRelationshipVO {

    private Long id;
    private Long ownerId;
    private String ownerName;
    private Long friendUserId;
    private String friendName;
    private Long groupId;
    private String groupName;
    private GroupType groupType;
    private String groupColor;
    private RelationshipStatus status;
    private Boolean inviteEmailSent;
    private OffsetDateTime inviteEmailSentAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime respondedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public Long getFriendUserId() { return friendUserId; }
    public void setFriendUserId(Long friendUserId) { this.friendUserId = friendUserId; }
    public String getFriendName() { return friendName; }
    public void setFriendName(String friendName) { this.friendName = friendName; }
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public GroupType getGroupType() { return groupType; }
    public void setGroupType(GroupType groupType) { this.groupType = groupType; }
    public String getGroupColor() { return groupColor; }
    public void setGroupColor(String groupColor) { this.groupColor = groupColor; }
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
