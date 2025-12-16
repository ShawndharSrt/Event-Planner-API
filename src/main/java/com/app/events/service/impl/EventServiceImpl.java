package com.app.events.service.impl;

import com.app.events.dto.BudgetSummary;
import com.app.events.dto.EventStats;
import com.app.events.dto.EventWithStats;
import com.app.events.dto.RecentEvent;
import com.app.events.dto.TimelineItem;
import com.app.events.mapper.EventWithStatsMapper;
import com.app.events.mapper.RecentEventMapper;
import com.app.events.model.Event;
import com.app.events.model.Guest;
import com.app.events.model.enums.AlertCode;
import com.app.events.repository.EventRepository;
import com.app.events.repository.GuestRepository;
import com.app.events.service.BudgetService;
import com.app.events.service.EventService;
import com.app.events.service.NotificationService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final RecentEventMapper recentEventMapper;
    private final EventWithStatsMapper eventWithStatsMapper;
    private final BudgetService budgetService;
    private final GuestRepository guestRepository;
    private final NotificationService notificationService;

    @Override
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @Override
    public List<EventWithStats> getAllEventsWithStats() {
        List<Event> events = eventRepository.findAll();

        return events.stream()
                .map(event -> {
                    EventStats stats = getEventStats(event.getId());
                    return eventWithStatsMapper.toEventWithStats(event, stats);
                })
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Event> getEventById(String id) {
        return eventRepository.findById(id);
    }

    @Override
    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }

    @Override
    public Event updateEvent(String id, Event event) {
        if (eventRepository.existsById(id)) {
            event.setId(id);
            return eventRepository.save(event);
        }
        throw new RuntimeException("Event not found with id: " + id);
    }

    @Override
    public void deleteEvent(String id) {
        eventRepository.deleteById(id);
    }

    @Override
    public List<RecentEvent> getRecentEvents(int limit) {
        Sort sort = Sort.by(Sort.Direction.DESC, "startDate");
        List<Event> events = eventRepository.findAll(sort);

        return recentEventMapper.toDtoList(events)
                .stream()
                .limit(Math.max(limit, 0))
                .toList();
    }

    @Override
    public EventStats getEventStats(String eventId) {
        return eventRepository.findById(eventId)
                .map(event -> {
                    List<Event.EventGuest> guests = event.getGuests();
                    int totalGuests = guests.size();
                    int confirmed = (int) guests.stream()
                            .filter(g -> "confirmed".equalsIgnoreCase(g.getStatus()))
                            .count();
                    int pending = (int) guests.stream()
                            .filter(g -> "pending".equalsIgnoreCase(g.getStatus()))
                            .count();
                    int declined = (int) guests.stream()
                            .filter(g -> "declined".equalsIgnoreCase(g.getStatus()))
                            .count();
                    return new EventStats(totalGuests, confirmed, pending, declined);
                })
                .orElse(new EventStats(0, 0, 0, 0));
    }

    @Override
    public Event addGuestsToEvent(String eventId, List<String> guestIds) {
        Event event = getEventById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));

        if (guestIds == null || guestIds.isEmpty()) {
            return event;
        }

        List<Guest> guests = guestRepository.findAllById(guestIds);

        for (Guest masterGuest : guests) {
            Optional<Event.EventGuest> existingGuestOpt = event.getGuests().stream()
                    .filter(g -> g.getGuestId().equals(masterGuest.getId()))
                    .findFirst();

            Event.EventGuest targetGuest;
            if (existingGuestOpt.isPresent()) {
                targetGuest = existingGuestOpt.get();
            } else {
                targetGuest = new Event.EventGuest();
                targetGuest.setGuestId(masterGuest.getId());
                targetGuest.setStatus("Pending");
                event.getGuests().add(targetGuest);
            }

            // Sync/Hydrate fields from Master Guest
            String fullName = (masterGuest.getFirstName() != null ? masterGuest.getFirstName() : "") + " " +
                    (masterGuest.getLastName() != null ? masterGuest.getLastName() : "");
            targetGuest.setName(fullName.trim());
            targetGuest.setEmail(masterGuest.getEmail());
            targetGuest.setGroup(masterGuest.getGroup());
            targetGuest.setDietary(masterGuest.getDietary());
            targetGuest.setNotes(masterGuest.getNotes());
        }
        Event saved = eventRepository.save(event);
        // Alert EITA: "Invitation Triggered Alert" for added guests
        if (!guestIds.isEmpty()) {
            // We could check if guests were actually added (vs existing), but for MVP just
            // alert on action.
            notificationService.createAlert(AlertCode.EITA, null, eventId,
                    "(" + guestIds.size() + " guests invited)");
        }
        return saved;
    }

    @Override
    public Event removeGuestFromEvent(String eventId, String guestId) {
        Event event = getEventById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));
        event.getGuests().removeIf(g -> g.getGuestId().equals(guestId));
        return eventRepository.save(event);
    }

    @Override
    public Event updateGuestStatus(String eventId, String guestId, String status) {
        Event event = getEventById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));
        event.getGuests().stream()
                .filter(g -> g.getGuestId().equals(guestId))
                .findFirst()
                .ifPresent(g -> g.setStatus(status));
        return eventRepository.save(event);
    }

    @Override
    public List<TimelineItem> getEventTimeline(String eventId) {
        return eventRepository.findById(eventId)
                .map(event -> {
                    List<TimelineItem> timeline = new java.util.ArrayList<>();

                    if (event.getStartTime() != null) {
                        timeline.add(new TimelineItem(
                                event.getStartTime(),
                                "Event Start",
                                event.getLocation() != null ? event.getLocation() : ""));
                    }

                    if (event.getEndTime() != null) {
                        timeline.add(new TimelineItem(
                                event.getEndTime(),
                                "Event End",
                                event.getLocation() != null ? event.getLocation() : ""));
                    }

                    return timeline;
                })
                .orElse(java.util.Collections.emptyList());
    }

    @Override
    public BudgetSummary getEventBudgetSummary(String eventId) {
        try {
            return budgetService.getBudgetSummaryByEventId(eventId);
        } catch (RuntimeException e) {
            // Return empty summary if no budget exists
            return new BudgetSummary(null, eventId, 0.0, 0.0, "USD", java.util.Collections.emptyList());
        }
    }
}
