package com.homecentral.friend.service;

import com.homecentral.friend.api.enums.RelationshipStatus;
import com.homecentral.friend.api.vo.FriendRelationshipVO;

import java.util.List;

public interface IFriendRelationshipService {

    FriendRelationshipVO invite(Long ownerId, Long friendUserId, Long groupId);

    FriendRelationshipVO accept(Long relationshipId, Long currentUserId);

    FriendRelationshipVO reject(Long relationshipId, Long currentUserId);

    void unbind(Long relationshipId, Long currentUserId);

    void block(Long relationshipId, Long currentUserId);

    List<FriendRelationshipVO> listMine(Long ownerId, RelationshipStatus status);

    List<FriendRelationshipVO> listIncoming(Long currentUserId);

    FriendRelationshipVO getById(Long relationshipId);
}
