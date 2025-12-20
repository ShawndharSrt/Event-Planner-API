package com.app.events.dto;

import lombok.Data;
import java.util.List;

@Data
public class CalendarResponseDto {
    private List<CalendarEventDto> events;
    private List<CalendarTaskDto> tasks;
}
