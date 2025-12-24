package com.app.events.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.mapping.Document;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "notification")
public class Notification extends BaseEntity {
    private String eventId;
    private String userId; // remove
    private String message;
    private String code; // Per Alert unique code
    private String severity;
    private String type;
    private String subType;
    private boolean read; // change to status field
    private LocalDateTime closedAt;
    private String redirectTo;

    // alertInfo list field
    // objects -> id, status, alert creation date field, desc, User Alert Info list
    // User Alert Info list -> objects -> userId, status, isEmail, isEmailTriggered
}
