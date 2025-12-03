package com.app.events.service.impl;

import com.app.events.dto.RecentEvent;
import com.app.events.mapper.RecentEventMapper;
import com.app.events.model.Event;
import com.app.events.repository.EventRepository;
import com.app.events.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final RecentEventMapper recentEventMapper;

    @Override
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
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
}
