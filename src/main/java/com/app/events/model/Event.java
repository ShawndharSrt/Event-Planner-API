package com.app.events.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "event")
public class Event extends BaseEntity {
    private String title;
    private String type;
    private String status;
    private LocalDate startDate;
    private String startTime;
    private LocalDate endDate;
    private String endTime;
    private String location;
    private String description;
    private String coverImage;
}
