package com.app.events.service;

import com.app.events.model.Notification;
import java.util.List;

public interface NotificationService {
    List<Notification> getNotificationsByUser(String userId);
    List<Notification> getNotificationsByEvent(String eventId);
    Notification markAsRead(String id);
}

