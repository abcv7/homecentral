package com.homecentral.friend.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "邀请好友/确认/拒绝/解除请求")
public class FriendRelationshipActionRequest {

    @Schema(description = "关系 ID（accept/reject/unbind/block 必填）")
    private Long relationshipId;

    @Schema(description = "分组 ID（invite 时必填）")
    private Long groupId;

    @Schema(description = "对方 userId（invite 时必填）")
    private Long friendUserId;

    public Long getRelationshipId() { return relationshipId; }
    public void setRelationshipId(Long relationshipId) { this.relationshipId = relationshipId; }
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public Long getFriendUserId() { return friendUserId; }
    public void setFriendUserId(Long friendUserId) { this.friendUserId = friendUserId; }
}
