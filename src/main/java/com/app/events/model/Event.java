package com.app.events.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "event")
public class Event extends BaseEntity {
    private String title;
    private String type;
    private String status;
    @Indexed
    private LocalDate startDate;
    private String startTime;
    private LocalDate endDate;
    private String endTime;
    private String location;
    private String description;
    private String coverImage;
    private String color;
    private List<EventGuest> guests = new ArrayList<>();

    @Data
    public static class EventGuest {
        private String guestId;
        private String name; // caching name for faster display
        private String email;
        private String group;
        private String status;
        private String dietary;
        private String notes;
    }
}
