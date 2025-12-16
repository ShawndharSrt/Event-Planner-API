package com.app.events.service;

import com.app.events.model.Notification;
import com.app.events.model.enums.AlertCode;

import java.util.List;

public interface NotificationService {
    List<Notification> getNotificationsByUser(String userId);

    List<Notification> getNotificationsByEvent(String eventId);

    Notification markAsRead(String id);

    void createAlert(AlertCode alertCode, String userId, String eventId,
            String messageSuffix);

    void closeAlert(String alertId);

    int markAllAsRead(String userId);
}
