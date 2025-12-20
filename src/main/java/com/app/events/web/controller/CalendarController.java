package com.app.events.web.controller;

import com.app.events.dto.ApiResponse;
import com.app.events.dto.CalendarResponseDto;
import com.app.events.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping
    public ResponseEntity<ApiResponse<CalendarResponseDto>> getCalendarData(
            @RequestParam String viewType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String eventId) {

        CalendarResponseDto data = calendarService.getCalendarData(viewType, startDate, endDate, eventId);
        return ResponseEntity.ok(ApiResponse.success("Calendar data fetched", data));
    }
}
