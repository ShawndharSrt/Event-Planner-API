package com.app.events.service.impl;

import com.app.events.dto.CalendarEventDto;
import com.app.events.dto.CalendarResponseDto;
import com.app.events.dto.CalendarTaskDto;
import com.app.events.dto.EventDropdownDto;
import com.app.events.model.Event;
import com.app.events.model.Task;
import com.app.events.repository.EventRepository;
import com.app.events.repository.TaskRepository;
import com.app.events.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService {

    private final EventRepository eventRepository;
    private final TaskRepository taskRepository;

    private String getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof String) {
            return (String) principal;
        }
        // Fallback or throw exception if principal is not string (e.g. UserDetails)
        // Based on user context, it seems to be a userId string.
        return principal.toString();
    }

    @Override
    public List<EventDropdownDto> getDropdownEvents() {
        String userId = getCurrentUserId();
        List<Event> events = eventRepository.findByCreatedBy(userId);
        return events.stream().map(event -> {
            EventDropdownDto dto = new EventDropdownDto();
            dto.setId(event.getId());
            dto.setName(event.getTitle());
            dto.setColor(event.getColor());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public CalendarResponseDto getCalendarData(String viewType, LocalDate startDate, LocalDate endDate,
            String eventId) {
        String userId = getCurrentUserId();
        CalendarResponseDto response = new CalendarResponseDto();

        List<CalendarEventDto> eventDtos = new ArrayList<>();
        List<CalendarTaskDto> taskDtos;

        // Fetch Events
        if (eventId != null && !eventId.isEmpty()) {
            Event event = eventRepository.findById(eventId).orElse(null);
            if (event != null && userId.equals(event.getCreatedBy())) {
                eventDtos.add(mapToCalendarEventDto(event));
            }
        } else {
            List<Event> events = eventRepository.findByCreatedBy(userId);
            eventDtos = events.stream().map(this::mapToCalendarEventDto).collect(Collectors.toList());
        }

        // Fetch Tasks
        List<Task> tasks;
        if (eventId != null && !eventId.isEmpty()) {
            tasks = taskRepository.findByCreatedByAndEventIdAndDueDateBetween(userId, eventId, startDate, endDate);
        } else {
            tasks = taskRepository.findByCreatedByAndDueDateBetween(userId, startDate, endDate);
        }

        taskDtos = tasks.stream()
                .map(task -> mapToCalendarTaskDto(task, viewType))
                .collect(Collectors.toList());

        response.setEvents(eventDtos);
        response.setTasks(taskDtos);

        return response;
    }

    private CalendarEventDto mapToCalendarEventDto(Event event) {
        CalendarEventDto dto = new CalendarEventDto();
        dto.setId(event.getId());
        dto.setName(event.getTitle());
        dto.setStartDate(event.getStartDate());
        dto.setEndDate(event.getEndDate());
        dto.setColor(event.getColor());
        return dto;
    }

    private CalendarTaskDto mapToCalendarTaskDto(Task task, String viewType) {
        CalendarTaskDto dto = new CalendarTaskDto();
        dto.setId(task.getId());
        dto.setEventId(task.getEventId());
        dto.setTitle(task.getTitle());
        dto.setDueDate(task.getDueDate());
        dto.setStatus(task.getStatus());

        if ("WEEK".equalsIgnoreCase(viewType) || "DAY".equalsIgnoreCase(viewType)) {
            dto.setDescription(task.getDescription());
            dto.setPriority(task.getPriority());
        }

        return dto;
    }
}
