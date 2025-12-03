package com.app.events.service.impl;

import com.app.events.dto.DashboardTask;
import com.app.events.mapper.DashboardTaskMapper;
import com.app.events.model.Task;
import com.app.events.repository.TaskRepository;
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
    public Task updateTask(String id, Task task) {
        if (taskRepository.existsById(id)) {
            task.setId(id);
            return taskRepository.save(task);
        }
        throw new RuntimeException("Task not found with id: " + id);
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
