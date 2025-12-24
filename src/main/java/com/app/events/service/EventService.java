package com.app.events.service;

import com.app.events.dto.BudgetSummary;
import com.app.events.dto.EventStats;
import com.app.events.dto.EventWithStats;
import com.app.events.dto.GuestImportResponse;
import com.app.events.dto.RecentEvent;
import com.app.events.dto.TimelineItem;
import com.app.events.model.Event;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface EventService {
    List<Event> getAllEvents();

    List<EventWithStats> getAllEventsWithStats();

    Optional<Event> getEventById(String id);

    Event createEvent(Event event);

    Event updateEvent(String id, Event event);

    void deleteEvent(String id);

    List<RecentEvent> getRecentEvents(int limit);

    EventStats getEventStats(String eventId);

    List<TimelineItem> getEventTimeline(String eventId);

    BudgetSummary getEventBudgetSummary(String eventId);

    Event addGuestsToEvent(String eventId, List<String> guestIds);

    Event removeGuestFromEvent(String eventId, String guestId);

    Event updateGuestStatus(String eventId, String guestId, String status);

    GuestImportResponse importGuestsFromExcel(MultipartFile file);

}
