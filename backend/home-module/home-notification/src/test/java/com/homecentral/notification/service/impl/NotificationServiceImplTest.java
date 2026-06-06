package com.homecentral.notification.service.impl;

import com.homecentral.notification.api.vo.NotificationVO;
import com.homecentral.notification.entity.Notification;
import com.homecentral.notification.mapper.NotificationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification createNotification(Long id, Long userId, boolean read) {
        Notification n = new Notification();
        n.setId(id);
        n.setTitle("通知标题");
        n.setContent("通知内容");
        n.setRead(read);
        n.setUserId(userId);
        return n;
    }

    @Test
    void shouldListNotifications() {
        when(notificationMapper.selectList(any()))
                .thenReturn(List.of(createNotification(1L, 1L, false)));

        List<NotificationVO> list = notificationService.listNotifications(1L);

        assertEquals(1, list.size());
        assertFalse(list.getFirst().isRead());
    }

    @Test
    void shouldReturnEmptyListWhenNoNotifications() {
        when(notificationMapper.selectList(any())).thenReturn(List.of());

        List<NotificationVO> list = notificationService.listNotifications(1L);

        assertTrue(list.isEmpty());
    }

    @Test
    void shouldMarkAsRead() {
        Notification notification = createNotification(1L, 1L, false);
        when(notificationMapper.selectById(1L)).thenReturn(notification);

        NotificationVO vo = notificationService.markAsRead(1L, 1L);

        assertTrue(vo.isRead());
        verify(notificationMapper).updateById(notification);
    }

    @Test
    void shouldThrowWhenMarkAsReadNonExistent() {
        when(notificationMapper.selectById(999L)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> notificationService.markAsRead(999L, 1L));
        assertEquals("通知不存在", ex.getMessage());
    }

    @Test
    void shouldMarkAllAsRead() {
        notificationService.markAllAsRead(1L);
        verify(notificationMapper).update(any(), any());
    }
}
