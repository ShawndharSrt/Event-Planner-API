package com.app.events.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventGuestResponse {
    // From Guest
    private String guestId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    // From GuestEvent
    private String group;
    private String status;
    private String dietary;
    private String notes;
}
