package com.app.events.mapper;

import com.app.events.dto.DashboardTask;
import com.app.events.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DashboardTaskMapper {

    @Mapping(target = "completed", expression = "java(\"completed\".equalsIgnoreCase(task.getStatus()))")
    DashboardTask toDto(Task task);

    List<DashboardTask> toDtoList(List<Task> tasks);
}


