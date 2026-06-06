package com.homecentral.friend.api.feign;

import com.homecentral.common.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "home-friend", path = "/api/friend")
public interface FriendClient {

    @GetMapping("/internal/visible-users")
    Result<List<Long>> getVisibleUsers(@RequestHeader(value = "X-User-Id", required = false) Long userId);

    @GetMapping("/internal/group-members")
    Result<List<Long>> getGroupMembers(@RequestParam("groupId") Long groupId);

    @GetMapping("/internal/user-groups")
    Result<List<Long>> getUserGroups(@RequestHeader(value = "X-User-Id", required = false) Long userId);

    @GetMapping("/internal/relationship-between")
    Result<Boolean> hasRelationshipBetween(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam("otherUserId") Long otherUserId);
}
