package com.app.events.service.impl;

import com.app.events.dto.BudgetSummary;
import com.app.events.dto.EventStats;
import com.app.events.dto.EventWithStats;
import com.app.events.dto.RecentEvent;
import com.app.events.dto.TimelineItem;
import com.app.events.mapper.EventWithStatsMapper;
import com.app.events.mapper.RecentEventMapper;
import com.app.events.model.Budget;
import com.app.events.model.Event;
import com.app.events.repository.BudgetRepository;
import com.app.events.repository.EventRepository;
import com.app.events.service.EventService;
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
    private final BudgetRepository budgetRepository;

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
    public Event addGuestToEvent(String eventId, Event.EventGuest guest) {
        Event event = getEventById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));
        // Check if guest already exists
        boolean exists = event.getGuests().stream()
                .anyMatch(g -> g.getGuestId().equals(guest.getGuestId()));
        if (!exists) {
            event.getGuests().add(guest);
            return eventRepository.save(event);
        }
        return event;
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
        Optional<Budget> budgetOpt = budgetRepository.findByEventId(eventId);

        if (budgetOpt.isPresent()) {
            Budget budget = budgetOpt.get();
            return new BudgetSummary(budget.getTotalBudget(), budget.getSpent());
        }

        // Return zero values if no budget exists
        return new BudgetSummary(0.0, 0.0);
    }
}
