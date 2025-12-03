package com.app.events.service;

import com.app.events.dto.DashboardTask;
import com.app.events.model.Task;
import java.util.List;
import java.util.Optional;

public interface TaskService {
    List<Task> getAllTasks();

    List<Task> getTasksByEventId(String eventId);

    Optional<Task> getTaskById(String id);

    Task createTask(Task task);

    Task updateTask(String id, Task task);

    void deleteTask(String id);

    List<DashboardTask> getDashboardTasks(int limit);
}
