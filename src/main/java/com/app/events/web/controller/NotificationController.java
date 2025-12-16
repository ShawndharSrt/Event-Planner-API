package com.app.events.web.controller;

import com.app.events.dto.ApiResponse;
import com.app.events.model.Notification;
import com.app.events.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {
    private final NotificationService notificationService;

    // /api/notifications?userId=...
    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> getNotifications(
            @RequestParam(required = false) String userId, @RequestParam(required = false) String eventId) {
        List<Notification> result = (eventId != null) ? notificationService.getNotificationsByEvent(eventId)
                : notificationService.getNotificationsByUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Notifications fetched", result));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Notification>> markAsRead(@PathVariable String id) {
        Notification notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", notification));
    }

    @PatchMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> markAllAsRead() {
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        int updatedCount = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read",
                Map.of("updatedCount", updatedCount)));
    }
}
