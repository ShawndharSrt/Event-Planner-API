package com.app.events.mapper;

import com.app.events.dto.RecentEvent;
import com.app.events.model.Event;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecentEventMapper {

    RecentEvent toRecentEventList(Event event);

    List<RecentEvent> toRecentEventList(List<Event> events);
}


