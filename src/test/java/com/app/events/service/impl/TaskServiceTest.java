package com.app.events.service.impl;

import com.app.events.dto.DashboardTask;
import com.app.events.dto.TaskUpdateRequest;
import com.app.events.mapper.DashboardTaskMapper;
import com.app.events.mapper.TaskMapper;
import com.app.events.model.Task;
import com.app.events.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private DashboardTaskMapper dashboardTaskMapper;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task task;

    @BeforeEach
    void setUp() {
        task = new Task();
        task.setId("tsk-1");
        task.setTitle("Setup Venue");
        task.setEventId("evt-1");
        task.setStatus("pending");
    }

    @Test
    void createTask_shouldSaveTask() {
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        Task created = taskService.createTask(task);

        assertNotNull(created);
        assertEquals("tsk-1", created.getId());
        verify(taskRepository).save(task);
    }

    @Test
    void getAllTasks_shouldReturnList() {
        when(taskRepository.findAll()).thenReturn(Collections.singletonList(task));

        assertEquals(1, taskService.getAllTasks().size());
    }

    @Test
    void getTasksByEventId_shouldReturnList() {
        when(taskRepository.findByEventId("evt-1")).thenReturn(Collections.singletonList(task));

        assertEquals(1, taskService.getTasksByEventId("evt-1").size());
    }

    @Test
    void getTaskById_shouldReturnTask() {
        when(taskRepository.findById("tsk-1")).thenReturn(Optional.of(task));

        Optional<Task> found = taskService.getTaskById("tsk-1");

        assertTrue(found.isPresent());
        assertEquals("tsk-1", found.get().getId());
    }

    @Test
    void updateTask_shouldUpdateWhenFound() {
        TaskUpdateRequest updateRequest = new TaskUpdateRequest();
        updateRequest.setTitle("Updated");

        when(taskRepository.findById("tsk-1")).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        doNothing().when(taskMapper).updateTaskFromDto(any(TaskUpdateRequest.class), any(Task.class));

        Task updated = taskService.updateTask("tsk-1", updateRequest);

        assertNotNull(updated);
        verify(taskMapper).updateTaskFromDto(updateRequest, task);
        verify(taskRepository).save(task);
    }

    @Test
    void deleteTask_shouldDeleteById() {
        doNothing().when(taskRepository).deleteById("tsk-1");

        taskService.deleteTask("tsk-1");

        verify(taskRepository).deleteById("tsk-1");
    }

    @Test
    void getDashboardTasks_shouldReturnLimitedList() {
        DashboardTask dashboardTask = new DashboardTask();
        dashboardTask.setId("tsk-1");

        when(taskRepository.findByStatusNot("completed")).thenReturn(Collections.singletonList(task));
        when(dashboardTaskMapper.toDtoList(anyList())).thenReturn(Collections.singletonList(dashboardTask));

        List<DashboardTask> result = taskService.getDashboardTasks(5);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
