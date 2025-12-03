package com.app.events.controller;

import com.app.events.dto.ApiResponse;
import com.app.events.dto.DashboardTask;
import com.app.events.dto.RecentEvent;
import com.app.events.service.EventService;
import com.app.events.service.GuestService;
import com.app.events.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
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

    @GetMapping("/recent-events")
    public ResponseEntity<ApiResponse<List<RecentEvent>>> getRecentEvents() {
        List<RecentEvent> recentEvents = eventService.getRecentEvents(3);
        return ResponseEntity.ok(ApiResponse.success("Recent events fetched", recentEvents));
    }

    @GetMapping("/tasks")
    public ResponseEntity<ApiResponse<List<DashboardTask>>> getTasks() {
        List<DashboardTask> tasks = taskService.getDashboardTasks(3);
        return ResponseEntity.ok(ApiResponse.success("Dashboard tasks fetched", tasks));
    }
}
