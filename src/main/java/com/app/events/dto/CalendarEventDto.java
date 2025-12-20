package com.app.events.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CalendarEventDto {
    private String id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String color;
}
