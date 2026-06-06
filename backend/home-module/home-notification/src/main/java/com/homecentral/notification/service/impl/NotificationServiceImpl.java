package com.homecentral.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homecentral.notification.api.vo.NotificationVO;
import com.homecentral.notification.entity.Notification;
import com.homecentral.notification.mapper.NotificationMapper;
import com.homecentral.notification.service.INotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationServiceImpl implements INotificationService {

    private final NotificationMapper notificationMapper;

    public NotificationServiceImpl(NotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    @Override
    public List<NotificationVO> listNotifications(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(Notification::getUserId, userId);
        }
        wrapper.orderByDesc(Notification::getNotifyTime);
        return notificationMapper.selectList(wrapper).stream().map(this::toVO).toList();
    }

    @Override
    @Transactional
    public NotificationVO markAsRead(Long id, Long userId) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new RuntimeException("通知不存在");
        }
        notification.setRead(true);
        notificationMapper.updateById(notification);
        return toVO(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        Notification notification = new Notification();
        notification.setRead(true);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getRead, false);
        notificationMapper.update(notification, wrapper);
    }

    private NotificationVO toVO(Notification n) {
        NotificationVO vo = new NotificationVO();
        vo.setId(n.getId());
        vo.setTitle(n.getTitle());
        vo.setContent(n.getContent());
        vo.setRead(n.getRead());
        vo.setNotifyTime(n.getNotifyTime());
        return vo;
    }
}
