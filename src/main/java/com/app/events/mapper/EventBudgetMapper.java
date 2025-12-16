package com.app.events.mapper;

import com.app.events.dto.BudgetUpdateRequest;
import com.app.events.model.EventBudget;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EventBudgetMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateBudgetFromDto(BudgetUpdateRequest dto, @MappingTarget EventBudget entity);

    EventBudget toEntity(BudgetUpdateRequest dto);
}
