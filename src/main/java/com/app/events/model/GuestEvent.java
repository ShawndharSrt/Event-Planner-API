package com.app.events.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "guestEvents")
public class GuestEvent extends BaseEntity {
    private String guestId;
    private String eventId;
    private String group;
    private String status;
    private String dietary;
    private String notes;
}
