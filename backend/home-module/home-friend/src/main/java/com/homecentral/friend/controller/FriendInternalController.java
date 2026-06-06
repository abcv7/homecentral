package com.homecentral.friend.controller;

import com.homecentral.common.model.Result;
import com.homecentral.friend.service.impl.FriendRelationshipServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "好友内部 API", description = "供其他微服务经网关调用的查询接口")
@RestController
@RequestMapping("/api/friend/internal")
public class FriendInternalController {

    private final FriendRelationshipServiceImpl relService;

    public FriendInternalController(FriendRelationshipServiceImpl relService) {
        this.relService = relService;
    }

    @Operation(summary = "返回 owner 全部 ACCEPTED 关系的 friendUserId")
    @GetMapping("/visible-users")
    public Result<List<Long>> visibleUsers(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(relService.getVisibleUserIds(userId));
    }

    @Operation(summary = "返回分组（ACCEPTED）成员 userId 列表")
    @GetMapping("/group-members")
    public Result<List<Long>> groupMembers(@RequestParam("groupId") Long groupId) {
        return Result.ok(relService.getGroupMemberIds(groupId));
    }

    @Operation(summary = "返回 owner 的分组 ID 列表")
    @GetMapping("/user-groups")
    public Result<List<Long>> userGroups(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(relService.getUserGroupIds(userId));
    }

    @Operation(summary = "判断两个用户之间是否存在 ACCEPTED 关系")
    @GetMapping("/relationship-between")
    public Result<Boolean> relationshipBetween(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                                 @RequestParam("otherUserId") Long otherUserId) {
        return Result.ok(relService.hasAcceptedRelationship(userId, otherUserId));
    }
}
