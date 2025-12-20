package com.app.events.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CalendarTaskDto {
    private String id;
    private String eventId;
    private String title;
    private String description;
    private LocalDate dueDate;
    private String status;
    private String priority;
}
