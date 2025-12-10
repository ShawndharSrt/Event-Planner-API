package com.app.events.web.controller;

import com.app.events.dto.ApiResponse;
import com.app.events.model.Task;
import com.app.events.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Task>>> getAllTasks(@RequestParam(required = false) String eventId) {
        List<Task> tasks = (eventId != null) ? taskService.getTasksByEventId(eventId) : taskService.getAllTasks();
        return ResponseEntity.ok(ApiResponse.success("Tasks fetched successfully", tasks));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<ApiResponse<Task>> getTaskById(@PathVariable String eventId) {
        return taskService.getTaskById(eventId)
                .map(task -> ResponseEntity.ok(ApiResponse.success("Task fetched successfully", task)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Task>> createTask(@RequestBody Task task) {
        return ResponseEntity.ok(ApiResponse.success("Task created successfully", taskService.createTask(task)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Task>> updateTask(@PathVariable String id, @RequestBody Task task) {
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", taskService.updateTask(id, task)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable String id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully", null));
    }
}
