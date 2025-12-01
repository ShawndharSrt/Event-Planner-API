package com.app.events.controller;

import com.app.events.dto.ApiResponse;
import com.app.events.model.Settings;
import com.app.events.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SettingsController {
    private final SettingsService settingsService;

    @GetMapping
    public ResponseEntity<ApiResponse<Settings>> getSettings(
            @RequestParam String userId, @RequestParam String eventId) {
        return settingsService.getSettings(userId, eventId)
            .map(settings -> ResponseEntity.ok(ApiResponse.success("Settings fetched", settings)))
            .orElse(ResponseEntity.ok(ApiResponse.success("No settings found", null)));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<Settings>> updateSettings(
            @RequestParam String userId, @RequestParam String eventId, @RequestBody Settings settings) {
        Settings saved = settingsService.upsertSettings(userId, eventId, settings);
        return ResponseEntity.ok(ApiResponse.success("Settings updated", saved));
    }
}
