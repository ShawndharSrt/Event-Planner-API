package com.app.events.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "task")
public class Task extends BaseEntity {
    private String eventId;
    private String title;
    private String description;
    private String assignee;
    private LocalDate dueDate;
    private String priority;
    private String status;
}
