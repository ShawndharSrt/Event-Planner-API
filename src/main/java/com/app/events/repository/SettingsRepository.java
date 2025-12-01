package com.app.events.repository;

import com.app.events.model.Settings;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SettingsRepository extends MongoRepository<Settings, String> {
    Optional<Settings> findByUserIdAndEventId(String userId, String eventId);
}

