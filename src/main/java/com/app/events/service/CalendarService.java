package com.app.events.service;

import com.app.events.dto.CalendarResponseDto;
import com.app.events.dto.EventDropdownDto;

import java.time.LocalDate;
import java.util.List;

public interface CalendarService {
    List<EventDropdownDto> getDropdownEvents();

    CalendarResponseDto getCalendarData(String viewType, LocalDate startDate, LocalDate endDate, String eventId);
}
