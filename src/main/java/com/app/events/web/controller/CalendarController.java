package com.app.events.web.controller;

import com.app.events.dto.ApiResponse;
import com.app.events.model.Event;
import com.app.events.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CalendarController {
    private final EventService eventService;

    @GetMapping("/events")
    public ResponseEntity<ApiResponse<List<Event>>> getCalendarEvents() {
        List<Event> events = eventService.getAllEvents();
        return ResponseEntity.ok(ApiResponse.success("Calendar events fetched", events));
    }
}
