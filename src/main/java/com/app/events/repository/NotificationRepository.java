package com.app.events.repository;

import com.app.events.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUserId(String userId);

    List<Notification> findByEventId(String eventId);

    boolean existsByUserIdAndCodeAndReadFalse(String userId, String code);

    boolean existsByEventIdAndCodeAndReadFalse(String eventId, String code);

    List<Notification> findByEventIdAndCodeAndReadFalse(String eventId, String code);
}
