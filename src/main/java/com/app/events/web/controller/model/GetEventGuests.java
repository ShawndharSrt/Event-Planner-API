package com.app.events.web.controller.model;

import com.app.events.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetEventGuests extends BaseEntity {

    private String guestId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String eventId;
    private String group;
    private String status;
    private String dietary;
    private String notes;
}
