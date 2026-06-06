package com.homecentral.friend.controller;

import com.homecentral.common.model.Result;
import com.homecentral.friend.api.dto.FriendRelationshipActionRequest;
import com.homecentral.friend.api.enums.RelationshipStatus;
import com.homecentral.friend.api.vo.FriendRelationshipVO;
import com.homecentral.friend.service.IFriendRelationshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "好友关系", description = "邀请/接受/拒绝/解除/block + 列表/收件箱")
@RestController
@RequestMapping("/api/friend/relationships")
public class FriendRelationshipController {

    private final IFriendRelationshipService relService;

    public FriendRelationshipController(IFriendRelationshipService relService) {
        this.relService = relService;
    }

    @Operation(summary = "邀请好友")
    @PostMapping("/invite")
    public Result<FriendRelationshipVO> invite(@Valid @RequestBody FriendRelationshipActionRequest request,
                                                @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(relService.invite(userId, request.getFriendUserId(), request.getGroupId()));
    }

    @Operation(summary = "接受邀请")
    @PostMapping("/accept")
    public Result<FriendRelationshipVO> accept(@Valid @RequestBody FriendRelationshipActionRequest request,
                                               @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(relService.accept(request.getRelationshipId(), userId));
    }

    @Operation(summary = "拒绝邀请")
    @PostMapping("/reject")
    public Result<FriendRelationshipVO> reject(@Valid @RequestBody FriendRelationshipActionRequest request,
                                               @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(relService.reject(request.getRelationshipId(), userId));
    }

    @Operation(summary = "解除关系")
    @PostMapping("/unbind")
    public Result<Void> unbind(@Valid @RequestBody FriendRelationshipActionRequest request,
                               @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        relService.unbind(request.getRelationshipId(), userId);
        return Result.<Void>ok(null);
    }

    @Operation(summary = "拉黑")
    @PostMapping("/block")
    public Result<Void> block(@Valid @RequestBody FriendRelationshipActionRequest request,
                              @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        relService.block(request.getRelationshipId(), userId);
        return Result.<Void>ok(null);
    }

    @Operation(summary = "我的关系列表（可按状态过滤）")
    @GetMapping
    public Result<List<FriendRelationshipVO>> list(@RequestParam(required = false) RelationshipStatus status,
                                                   @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(relService.listMine(userId, status));
    }

    @Operation(summary = "我的收件箱（待我接受）")
    @GetMapping("/incoming")
    public Result<List<FriendRelationshipVO>> incoming(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(relService.listIncoming(userId));
    }

    @Operation(summary = "按 ID 解除关系（REST 风格）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteById(@PathVariable("id") Long id,
                                   @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        relService.unbind(id, userId);
        return Result.<Void>ok(null);
    }
}
