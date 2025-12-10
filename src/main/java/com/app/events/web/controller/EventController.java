package com.app.events.web.controller;

import com.app.events.dto.*;
import com.app.events.model.Event;
import com.app.events.service.EventService;
import com.app.events.service.GuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allow all origins for now
public class EventController {

    private final EventService eventService;
    private final GuestService guestService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventWithStats>>> getAllEvents() {
        return ResponseEntity
                .ok(ApiResponse.success("Events fetched successfully", eventService.getAllEventsWithStats()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Event>> getEventById(@PathVariable String id) {
        return eventService.getEventById(id)
                .map(event -> ResponseEntity.ok(ApiResponse.success("Event fetched successfully", event)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Event>> createEvent(@RequestBody Event event) {
        return ResponseEntity.ok(ApiResponse.success("Event created successfully", eventService.createEvent(event)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Event>> updateEvent(@PathVariable String id, @RequestBody Event event) {
        return ResponseEntity
                .ok(ApiResponse.success("Event updated successfully", eventService.updateEvent(id, event)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable String id) {
        eventService.deleteEvent(id);
        return ResponseEntity.ok(ApiResponse.success("Event deleted successfully", null));
    }

    @GetMapping("/{id}/guests")
    public ResponseEntity<ApiResponse<List<EventGuestResponse>>> getGuestsByEventId(@PathVariable String id) {
        List<EventGuestResponse> s = guestService.getEventGuestsByEventId(id);
        return ResponseEntity.ok(ApiResponse.success("Event guests fetched successfully",
                guestService.getEventGuestsByEventId(id)));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<ApiResponse<EventStats>> getEventStats(@PathVariable String id) {
        return ResponseEntity
                .ok(ApiResponse.success("Event stats fetched successfully", eventService.getEventStats(id)));
    }

    @GetMapping("/{id}/timeline")
    public ResponseEntity<ApiResponse<List<TimelineItem>>> getEventTimeline(@PathVariable String id) {
        return ResponseEntity
                .ok(ApiResponse.success("Event timeline fetched successfully", eventService.getEventTimeline(id)));
    }

    @GetMapping("/{id}/budget/summary")
    public ResponseEntity<ApiResponse<BudgetSummary>> getEventBudgetSummary(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("Event budget summary fetched successfully",
                eventService.getEventBudgetSummary(id)));
    }
}
