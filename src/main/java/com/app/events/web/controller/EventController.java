package com.app.events.web.controller;

import com.app.events.dto.*;
import com.app.events.model.Event;
import com.app.events.service.CalendarService;
import com.app.events.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allow all origins for now
public class EventController {

    private final EventService eventService;
    private final CalendarService calendarService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventWithStats>>> getAllEvents() {
        return ResponseEntity
                .ok(ApiResponse.success("Events fetched successfully", eventService.getAllEventsWithStats()));
    }

    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<EventDropdownDto>>> getEventsDropdown() {
        return ResponseEntity.ok(ApiResponse.success("Dropdown events fetched", calendarService.getDropdownEvents()));
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
        return eventService.getEventById(id)
                .map(event -> {
                    List<EventGuestResponse> responses = event.getGuests().stream()
                            .map(g -> {
                                String[] names = g.getName() != null ? g.getName().split(" ", 2)
                                        : new String[] { "", "" };
                                return new EventGuestResponse(
                                        g.getGuestId(),
                                        names[0],
                                        names.length > 1 ? names[1] : "",
                                        g.getEmail(),
                                        "", // Phone not captured in embedded object currently
                                        g.getGroup(),
                                        g.getStatus(),
                                        g.getDietary(),
                                        g.getNotes());
                            })
                            .toList();
                    return ResponseEntity.ok(ApiResponse.success("Event guests fetched successfully", responses));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/guests")
    public ResponseEntity<ApiResponse<Event>> addGuestsToEvent(@PathVariable String id,
            @RequestBody AddGuestsRequest request) {
        return ResponseEntity
                .ok(ApiResponse.success("Guests added successfully",
                        eventService.addGuestsToEvent(id, request.getGuestIds())));
    }

    @DeleteMapping("/{id}/guests/{guestId}")
    public ResponseEntity<ApiResponse<Event>> removeGuestFromEvent(@PathVariable String id,
            @PathVariable String guestId) {
        return ResponseEntity
                .ok(ApiResponse.success("Guest removed successfully", eventService.removeGuestFromEvent(id, guestId)));
    }

    @PatchMapping("/{id}/guests/{guestId}")
    public ResponseEntity<ApiResponse<Event>> updateGuestStatus(@PathVariable String id, @PathVariable String guestId,
            @RequestBody Event.EventGuest statusUpdate) {
        // Assuming statusUpdate contains the new status in the 'status' field
        return ResponseEntity.ok(ApiResponse.success("Guest status updated successfully",
                eventService.updateGuestStatus(id, guestId, statusUpdate.getStatus())));
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

    @PostMapping("/upload-guests")
    public ResponseEntity<ApiResponse<GuestImportResponse>> uploadGuests(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success( "Excel uploaded successfully", eventService.importGuestsFromExcel(file)));
    }
}
