package com.app.events.service;

import com.app.events.model.Settings;
import java.util.Optional;

public interface SettingsService {
    Optional<Settings> getSettings(String userId, String eventId);
    Settings upsertSettings(String userId, String eventId, Settings settings);
}

