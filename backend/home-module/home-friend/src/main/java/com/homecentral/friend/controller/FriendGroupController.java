package com.homecentral.friend.controller;

import com.homecentral.common.model.Result;
import com.homecentral.friend.api.dto.FriendGroupRequest;
import com.homecentral.friend.api.vo.FriendGroupVO;
import com.homecentral.friend.service.IFriendGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "好友分组", description = "分组 CRUD（每用户独立 owner）")
@RestController
@RequestMapping("/api/friend/groups")
public class FriendGroupController {

    private final IFriendGroupService groupService;

    public FriendGroupController(IFriendGroupService groupService) {
        this.groupService = groupService;
    }

    @Operation(summary = "创建分组")
    @PostMapping
    public Result<FriendGroupVO> create(@Valid @RequestBody FriendGroupRequest request,
                                        @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(groupService.create(request, userId));
    }

    @Operation(summary = "更新分组")
    @PutMapping("/{groupId}")
    public Result<FriendGroupVO> update(@PathVariable Long groupId,
                                        @Valid @RequestBody FriendGroupRequest request,
                                        @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(groupService.update(groupId, request, userId));
    }

    @Operation(summary = "删除分组")
    @DeleteMapping("/{groupId}")
    public Result<Void> delete(@PathVariable Long groupId,
                               @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        groupService.delete(groupId, userId);
        return Result.<Void>ok(null);
    }

    @Operation(summary = "我的分组列表")
    @GetMapping
    public Result<List<FriendGroupVO>> list(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(groupService.listMine(userId));
    }

    @Operation(summary = "分组详情")
    @GetMapping("/{groupId}")
    public Result<FriendGroupVO> get(@PathVariable Long groupId,
                                     @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(groupService.getById(groupId, userId));
    }
}
