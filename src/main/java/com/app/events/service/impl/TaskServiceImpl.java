package com.app.events.service.impl;

import com.app.events.dto.DashboardTask;
import com.app.events.dto.TaskUpdateRequest;
import com.app.events.mapper.DashboardTaskMapper;
import com.app.events.mapper.TaskMapper;
import com.app.events.model.Task;
import com.app.events.model.enums.AlertCode;
import com.app.events.repository.TaskRepository;
import com.app.events.service.NotificationService;
import com.app.events.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final DashboardTaskMapper dashboardTaskMapper;
    private final TaskMapper taskMapper;
    private final NotificationService notificationService;

    @Override
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Override
    public List<Task> getTasksByEventId(String eventId) {
        return taskRepository.findByEventId(eventId);
    }

    @Override
    public Optional<Task> getTaskById(String id) {
        return taskRepository.findById(id);
    }

    @Override
    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    @Override
    public Task updateTask(String id, TaskUpdateRequest taskUpdateRequest) {
        return taskRepository.findById(id).map(existingTask -> {
            String oldStatus = existingTask.getStatus();
            taskMapper.updateTaskFromDto(taskUpdateRequest, existingTask);
            Task saved = taskRepository.save(existingTask);

            // Check for TCA (Task Completed Alert)
            if (!"completed".equalsIgnoreCase(oldStatus) && "completed".equalsIgnoreCase(saved.getStatus())) {
                // Prevent duplicate online alert if strictly required, but "completed" happens
                // once usually.
                // For stricter logic we could check existing alerts.
                notificationService.createAlert(AlertCode.TCA,
                        null, saved.getEventId(), ": " + saved.getTitle());
            }
            return saved;
        }).orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
    }

    @Override
    public void deleteTask(String id) {
        taskRepository.deleteById(id);
    }

    @Override
    public List<DashboardTask> getDashboardTasks(int limit) {
        List<Task> tasks = taskRepository.findByStatusNot("completed");

        return dashboardTaskMapper.toDtoList(tasks)
                .stream()
                .limit(Math.max(limit, 0))
                .toList();
    }
}
