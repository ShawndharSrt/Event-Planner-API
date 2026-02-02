package com.app.events.service.impl;

import com.app.events.model.Notification;
import com.app.events.model.enums.AlertCode;
import com.app.events.repository.NotificationRepository;
import com.app.events.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;

    @Override
    public List<Notification> getNotificationsByUser(String userId) {
        return notificationRepository.findByUserId(userId);
    }

    @Override
    public List<Notification> getNotificationsByEvent(String eventId) {
        return notificationRepository.findByEventId(eventId);
    }

    @Override
    public Notification markAsRead(String id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        if (notification.getClosedAt() == null) {
            notification.setClosedAt(LocalDateTime.now());
        }
        return notificationRepository.save(notification);
    }

    @Override
    public void createAlert(AlertCode alertCode, String userId, String eventId,
            String messageSuffix) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setEventId(eventId);
        notification.setCode(alertCode.getCode());
        notification.setSeverity(alertCode.getSeverity().name());
        notification.setType(alertCode.getType().name());
        notification.setSubType(alertCode.getSubType());
        notification.setMessage(alertCode.getDescription() + (messageSuffix != null ? " " + messageSuffix : ""));
        notification.setRead(false);
        // BaseEntity handles createdAt
        notificationRepository.save(notification);
    }

    @Override
    public void closeAlert(String alertId) {
        Notification notification = notificationRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found with id: " + alertId));
        notification.setRead(true);
        notification.setClosedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Override
    public int markAllAsRead(String userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndReadFalse(userId);
        unreadNotifications.stream().peek(data->{
            data.setRead(true);
            data.setClosedAt(LocalDateTime.now());
        });
        notificationRepository.saveAll(unreadNotifications);
        return unreadNotifications.size();
    }
}
