package com.app.events.controller;

import com.app.events.dto.ApiResponse;
import com.app.events.service.EventService;
import com.app.events.service.GuestService;
import com.app.events.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {
    private final EventService eventService;
    private final GuestService guestService;
    private final TaskService taskService;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOverview() {
        Map<String, Object> overview = new HashMap<>();
        overview.put("totalEvents", eventService.getAllEvents().size());
        overview.put("totalGuests", guestService.getAllGuests().size());
        overview.put("totalTasks", taskService.getAllTasks().size());
        return ResponseEntity.ok(ApiResponse.success("Dashboard overview fetched", overview));
    }
}
