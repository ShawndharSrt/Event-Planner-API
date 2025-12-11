package com.app.events.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TaskUpdateRequest {
    private String eventId;
    private String title;
    private String description;
    private String assignee;
    private LocalDate dueDate;
    private String priority;
    private String status;
}
