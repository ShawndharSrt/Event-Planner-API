package com.app.events.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "guest")
public class Guest extends BaseEntity {
    private String eventId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String group;
    private String status;
    private String dietary;
    private String notes;
}
