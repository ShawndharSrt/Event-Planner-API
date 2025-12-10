package com.app.events.mapper;

import com.app.events.dto.EventStats;
import com.app.events.dto.EventWithStats;
import com.app.events.model.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventWithStatsMapper {

    @Mapping(target = "stats", source = "stats")
    EventWithStats toEventWithStats(Event event, EventStats stats);
}
