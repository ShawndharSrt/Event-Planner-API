package com.app.events.web.controller;

import com.app.events.dto.AddGuestsRequest;
import com.app.events.dto.ApiResponse;
import com.app.events.dto.EventWithStats;
import com.app.events.model.Event;
import com.app.events.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.events.config.MongoConfig;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@WebMvcTest(value = EventController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
}, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MongoConfig.class))
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @Autowired
    private ObjectMapper objectMapper;

    private Event event;

    @BeforeEach
    void setUp() {
        event = new Event();
        event.setId("evt-123");
        event.setTitle("Summer Gala");
        event.setStartDate(LocalDate.of(2025, 7, 15));
        event.setLocation("Grand Hall");
    }

    @Test
    void createEvent_shouldReturnCreatedEvent() throws Exception {
        when(eventService.createEvent(any(Event.class))).thenReturn(event);

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("evt-123"))
                .andExpect(jsonPath("$.data.title").value("Summer Gala"));
    }

    @Test
    void getAllEvents_shouldReturnListOfEvents() throws Exception {
        EventWithStats eventWithStats = new EventWithStats();
        eventWithStats.setId(event.getId());
        eventWithStats.setTitle(event.getTitle());
        eventWithStats.setStartDate(event.getStartDate());
        eventWithStats.setLocation(event.getLocation());

        when(eventService.getAllEventsWithStats()).thenReturn(Collections.singletonList(eventWithStats));

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("evt-123"));
    }

    @Test
    void getEventById_shouldReturnEvent() throws Exception {
        when(eventService.getEventById("evt-123")).thenReturn(Optional.of(event));

        mockMvc.perform(get("/api/events/evt-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("evt-123"));
    }

    @Test
    void getEventById_shouldReturnNotFound_whenEventDoesNotExist() throws Exception {
        when(eventService.getEventById("evt-999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/events/evt-999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateEvent_shouldReturnUpdatedEvent() throws Exception {
        event.setLocation("New Venue");
        when(eventService.updateEvent(eq("evt-123"), any(Event.class))).thenReturn(event);

        mockMvc.perform(patch("/api/events/evt-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.location").value("New Venue"));
    }

    @Test
    void deleteEvent_shouldReturnSuccess() throws Exception {
        doNothing().when(eventService).deleteEvent("evt-123");

        mockMvc.perform(delete("/api/events/evt-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Event deleted successfully"));
    }

    @Test
    void addGuestsToEvent_shouldReturnEventWithGuests() throws Exception {
        AddGuestsRequest request = new AddGuestsRequest();
        request.setGuestIds(Arrays.asList("gst-1", "gst-2"));

        when(eventService.addGuestsToEvent(eq("evt-123"), any())).thenReturn(event);

        mockMvc.perform(post("/api/events/evt-123/guests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Guests added successfully"));
    }

    @Test
    void createEvent_shouldReturnBadRequest_whenDateRangeIsInvalid() throws Exception {
        event.setStartDate(LocalDate.of(2025, 8, 1));
        event.setEndDate(LocalDate.of(2025, 7, 1)); // End before Start

        // Assuming service or controller validator throws exception
        when(eventService.createEvent(any(Event.class))).thenThrow(new IllegalArgumentException("Invalid date range"));

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isBadRequest()) // This expects the global exception handler to map
                                                    // IllegalArgumentException to 400
                .andExpect(jsonPath("$.message").exists()); // Adjust based on actual error response structure
    }
}
