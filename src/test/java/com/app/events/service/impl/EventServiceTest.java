package com.app.events.service.impl;

import com.app.events.mapper.EventWithStatsMapper;
import com.app.events.mapper.RecentEventMapper;
import com.app.events.model.Event;
import com.app.events.model.Guest;
import com.app.events.repository.BudgetRepository;
import com.app.events.repository.EventRepository;
import com.app.events.repository.GuestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private GuestRepository guestRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private RecentEventMapper recentEventMapper;

    @Mock
    private EventWithStatsMapper eventWithStatsMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    private Event event;

    @BeforeEach
    void setUp() {
        event = new Event();
        event.setId("evt-123");
        event.setTitle("Summer Gala");
        event.setStartDate(LocalDate.of(2025, 7, 15));
        event.setGuests(new ArrayList<>());
    }

    @Test
    void createEvent_shouldSaveEvent() {
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        Event created = eventService.createEvent(event);

        assertNotNull(created);
        assertEquals("evt-123", created.getId());
        verify(eventRepository).save(event);
    }

    @Test
    void getEventById_shouldReturnEvent() {
        when(eventRepository.findById("evt-123")).thenReturn(Optional.of(event));

        Optional<Event> found = eventService.getEventById("evt-123");

        assertTrue(found.isPresent());
        assertEquals("evt-123", found.get().getId());
    }

    @Test
    void updateEvent_shouldUpdateWhenFound() {
        when(eventRepository.existsById("evt-123")).thenReturn(true);
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        Event updated = eventService.updateEvent("evt-123", event);

        assertNotNull(updated);
        verify(eventRepository).save(event);
    }

    @Test
    void updateEvent_shouldThrowWhenNotFound() {
        when(eventRepository.existsById("evt-999")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> eventService.updateEvent("evt-999", event));
    }

    @Test
    void deleteEvent_shouldDeleteById() {
        doNothing().when(eventRepository).deleteById("evt-123");

        eventService.deleteEvent("evt-123");

        verify(eventRepository).deleteById("evt-123");
    }

    @Test
    void addGuestsToEvent_shouldAddGuests() {
        Guest guest = new Guest();
        guest.setId("gst-1");
        guest.setFirstName("John");
        guest.setLastName("Doe");

        when(eventRepository.findById("evt-123")).thenReturn(Optional.of(event));
        when(guestRepository.findAllById(anyList())).thenReturn(Arrays.asList(guest));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        Event result = eventService.addGuestsToEvent("evt-123", Arrays.asList("gst-1"));

        assertNotNull(result);
        assertEquals(1, result.getGuests().size());
        assertEquals("gst-1", result.getGuests().get(0).getGuestId());
    }

    @Test
    void removeGuestFromEvent_shouldRemoveGuest() {
        Event.EventGuest eventGuest = new Event.EventGuest();
        eventGuest.setGuestId("gst-1");
        event.getGuests().add(eventGuest);

        when(eventRepository.findById("evt-123")).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        Event result = eventService.removeGuestFromEvent("evt-123", "gst-1");

        assertTrue(result.getGuests().isEmpty());
    }

    @Test
    void removeGuestFromEvent_shouldDoNothing_whenGuestNotFound() {
        Event.EventGuest eventGuest = new Event.EventGuest();
        eventGuest.setGuestId("gst-1");
        event.getGuests().add(eventGuest);

        when(eventRepository.findById("evt-123")).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        Event result = eventService.removeGuestFromEvent("evt-123", "gst-999"); // Non-existent guest

        assertEquals(1, result.getGuests().size());
        assertEquals("gst-1", result.getGuests().get(0).getGuestId());
    }

    @Test
    void addGuestsToEvent_shouldNotAddDuplicateGuest() {
        // Existing guest
        Event.EventGuest existingGuest = new Event.EventGuest();
        existingGuest.setGuestId("gst-1");
        event.getGuests().add(existingGuest);

        // Guest to add (same ID)
        Guest guest = new Guest();
        guest.setId("gst-1");

        when(eventRepository.findById("evt-123")).thenReturn(Optional.of(event));
        when(guestRepository.findAllById(anyList())).thenReturn(Arrays.asList(guest));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        Event result = eventService.addGuestsToEvent("evt-123", Arrays.asList("gst-1"));

        assertEquals(1, result.getGuests().size()); // Should still be 1, not 2
        assertEquals("gst-1", result.getGuests().get(0).getGuestId());
    }
}
