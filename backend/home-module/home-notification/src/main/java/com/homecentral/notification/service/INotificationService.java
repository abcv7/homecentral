package com.homecentral.notification.service;

import com.homecentral.notification.api.vo.NotificationVO;

import java.util.List;

public interface INotificationService {

    List<NotificationVO> listNotifications(Long userId);

    NotificationVO markAsRead(Long id, Long userId);

    void markAllAsRead(Long userId);
}
