package com.app.events.mapper;

import com.app.events.dto.GuestExcelDto;
import com.app.events.model.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring", imports = UUID.class)
public interface EventGuestMapper {

    @Mapping(target = "guestId", expression = "java(UUID.randomUUID().toString())")
    Event.EventGuest toEventGuest(GuestExcelDto dto);
}
