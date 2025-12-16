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
    private String userId;
    private String message;
    private String code;
    private String severity;
    private String type;
    private String subType;
    private boolean read;
    private LocalDateTime closedAt;
}
