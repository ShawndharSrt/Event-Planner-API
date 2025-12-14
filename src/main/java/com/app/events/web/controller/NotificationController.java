package com.app.events.web.controller;

import com.app.events.dto.ApiResponse;
import com.app.events.model.Notification;
import com.app.events.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Notification>> markAsRead(@PathVariable String id) {
        Notification notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", notification));
    }
}
