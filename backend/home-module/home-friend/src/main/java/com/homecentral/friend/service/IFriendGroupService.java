package com.homecentral.friend.service;

import com.homecentral.friend.api.dto.FriendGroupRequest;
import com.homecentral.friend.api.vo.FriendGroupVO;

import java.util.List;

public interface IFriendGroupService {

    FriendGroupVO create(FriendGroupRequest request, Long ownerId);

    FriendGroupVO update(Long groupId, FriendGroupRequest request, Long ownerId);

    void delete(Long groupId, Long ownerId);

    List<FriendGroupVO> listMine(Long ownerId);

    FriendGroupVO getById(Long groupId, Long ownerId);
}
