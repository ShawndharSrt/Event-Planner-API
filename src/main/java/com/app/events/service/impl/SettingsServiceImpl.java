package com.app.events.service.impl;

import com.app.events.model.Settings;
import com.app.events.repository.SettingsRepository;
import com.app.events.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SettingsServiceImpl implements SettingsService {
    private final SettingsRepository settingsRepository;

    @Override
    public Optional<Settings> getSettings(String userId, String eventId) {
        return settingsRepository.findByUserIdAndEventId(userId, eventId);
    }

    @Override
    public Settings upsertSettings(String userId, String eventId, Settings settings) {
        settings.setUserId(userId);
        settings.setEventId(eventId);
        return settingsRepository.save(settings);
    }
}

