package com.app.events.dto;

import com.app.events.model.Event;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EventWithStats extends Event {
    private EventStats stats;
}
