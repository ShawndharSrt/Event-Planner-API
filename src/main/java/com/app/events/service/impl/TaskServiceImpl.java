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
import com.app.events.util.ApiConstants;
import com.app.events.util.AppUtils;
import com.app.events.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final DashboardTaskMapper dashboardTaskMapper;
    private final TaskMapper taskMapper;
    private final NotificationService notificationService;

    @Override
    public List<Task> getAllTasks() {
        String currentUserId = AppUtils.getCurrentUserId();
        if (isAdmin(currentUserId)) {
            return taskRepository.findAll();
        }
        return taskRepository.findByCreatedBy(currentUserId);
    }

    @Override
    public List<Task> getTasksByEventId(String eventId) {
        String currentUserId = AppUtils.getCurrentUserId();
        // Here we could filter strictly by createdBy as well, or rely on event
        // ownership first.
        // Assuming we want strict separation:
        List<Task> tasks = taskRepository.findByEventId(eventId);
        if (isAdmin(currentUserId)) {
            return tasks;
        }
        return tasks.stream().filter(t -> currentUserId.equals(t.getCreatedBy())).toList();
    }

    @Override
    public Optional<Task> getTaskById(String id) {
        String currentUserId = AppUtils.getCurrentUserId();
        return taskRepository.findById(id)
                .filter(task -> isAdmin(currentUserId) || currentUserId.equals(task.getCreatedBy()));
    }

    @Override
    public Task createTask(Task task) {
        task.setCreatedBy(AppUtils.getCurrentUserId());
        return taskRepository.save(task);
    }

    @Override
    public Task updateTask(String id, TaskUpdateRequest taskUpdateRequest) {
        return taskRepository.findById(id).map(existingTask -> {
            String currentUserId = AppUtils.getCurrentUserId();
            if (!isAdmin(currentUserId) && !currentUserId.equals(existingTask.getCreatedBy())) {
                throw new RuntimeException("Unauthorized to update this task");
            }

            String oldStatus = existingTask.getStatus();
            taskMapper.updateTaskFromDto(taskUpdateRequest, existingTask);
            Task saved = taskRepository.save(existingTask);

            // Check for TCA (Task Completed Alert)
            if (!ApiConstants.COMPLETED.equalsIgnoreCase(oldStatus)
                    && ApiConstants.COMPLETED.equalsIgnoreCase(saved.getStatus())) {
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
        String currentUserId = AppUtils.getCurrentUserId();
        Task existing = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        if (!isAdmin(currentUserId) && !currentUserId.equals(existing.getCreatedBy())) {
            throw new RuntimeException("Unauthorized to delete this task");
        }
        taskRepository.deleteById(id);
    }

    @Override
    public List<DashboardTask> getDashboardTasks(int limit) {
        String currentUserId = AppUtils.getCurrentUserId();
        List<Task> tasks;
        if (isAdmin(currentUserId)) {
            tasks = taskRepository.findByStatusNot("completed");
        } else {
            tasks = taskRepository.findByCreatedByAndStatusNot(currentUserId, "completed");
        }

        return dashboardTaskMapper.toDtoList(tasks)
                .stream()
                .limit(Math.max(limit, 0))
                .toList();
    }

    @Override
    public long getCompletedTasksCount() {
        String currentUserId = AppUtils.getCurrentUserId();
        if (isAdmin(currentUserId)) {
            return taskRepository.countByStatus("done");
        }
        return taskRepository.countByCreatedByAndStatus(currentUserId, "done");
    }

    private boolean isAdmin(String userId) {
        if (userId == null)
            return false;
        return userRepository.findByUserId(userId)
                .map(com.app.events.model.User::getRole)
                .map(roles -> roles.stream().anyMatch(role -> "ADMIN".equalsIgnoreCase(role)))
                .orElse(false);
    }
}
