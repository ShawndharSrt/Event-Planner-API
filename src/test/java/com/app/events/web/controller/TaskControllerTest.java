package com.app.events.web.controller;

import com.app.events.config.MongoConfig;
import com.app.events.dto.TaskUpdateRequest;
import com.app.events.model.Task;
import com.app.events.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = TaskController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
}, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MongoConfig.class))
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

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
    void createTask_shouldReturnCreatedTask() throws Exception {
        when(taskService.createTask(any(Task.class))).thenReturn(task);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("tsk-1"))
                .andExpect(jsonPath("$.data.title").value("Setup Venue"));
    }

    @Test
    void getAllTasks_shouldReturnListOfTasks() throws Exception {
        when(taskService.getAllTasks()).thenReturn(Collections.singletonList(task));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("tsk-1"));
    }

    @Test
    void getTasksByEventId_shouldReturnListOfTasks() throws Exception {
        when(taskService.getTasksByEventId("evt-1")).thenReturn(Collections.singletonList(task));

        mockMvc.perform(get("/api/tasks")
                .param("eventId", "evt-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].eventId").value("evt-1"));
    }

    @Test
    void getTaskById_shouldReturnTask() throws Exception {
        when(taskService.getTaskById("tsk-1")).thenReturn(Optional.of(task));

        // Note: The controller mapping for getTaskById is actually
        // @GetMapping("/{eventId}")
        // but based on typical REST patterns and method name getTaskById, it implies ID
        // fetch.
        // Looking at the code: @GetMapping("/{eventId}") public
        // ResponseEntity<ApiResponse<Task>> getTaskById(@PathVariable String eventId)
        // This seems to be a misnomer in the controller implementation (parameter name
        // eventId but method says getTaskById).
        // Assuming the intention is to get a task by its ID.
        // I'll test based on the actual code path: /api/tasks/{id}

        mockMvc.perform(get("/api/tasks/tsk-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("tsk-1"));
    }

    @Test
    void updateTask_shouldReturnUpdatedTask() throws Exception {
        TaskUpdateRequest updateRequest = new TaskUpdateRequest();
        updateRequest.setTitle("Updated Title");
        task.setTitle("Updated Title");

        when(taskService.updateTask(eq("tsk-1"), any(TaskUpdateRequest.class))).thenReturn(task);

        mockMvc.perform(patch("/api/tasks/tsk-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated Title"));
    }

    @Test
    void deleteTask_shouldReturnSuccess() throws Exception {
        doNothing().when(taskService).deleteTask("tsk-1");

        mockMvc.perform(delete("/api/tasks/tsk-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Task deleted successfully"));
    }
}
