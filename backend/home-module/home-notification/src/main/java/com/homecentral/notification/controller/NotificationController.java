package com.homecentral.notification.controller;

import com.homecentral.common.model.Result;
import com.homecentral.notification.api.vo.NotificationVO;
import com.homecentral.notification.service.INotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "通知管理", description = "通知列表查询和已读标记")
@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    private final INotificationService notificationService;

    public NotificationController(INotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "通知列表", description = "获取当前用户的通知列表")
    @GetMapping
    public Result<List<NotificationVO>> list(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(notificationService.listNotifications(userId));
    }

    @Operation(summary = "标记已读", description = "将指定通知标记为已读")
    @PutMapping("/{id}/read")
    public Result<NotificationVO> markAsRead(@PathVariable Long id,
                                            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(notificationService.markAsRead(id, userId));
    }

    @Operation(summary = "全部已读", description = "将所有通知标记为已读")
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        notificationService.markAllAsRead(userId);
        return Result.ok(null);
    }
}
