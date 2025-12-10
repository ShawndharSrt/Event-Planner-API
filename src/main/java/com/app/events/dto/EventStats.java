package com.app.events.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventStats {
    private int totalGuests;
    private int confirmed;
    private int pending;
    private int declined;
}
