package com.app.events.web.controller;

import com.app.events.dto.ApiResponse;
import com.app.events.dto.UserStats;
import com.app.events.model.User;
import com.app.events.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success("Users fetched successfully", userService.getAllUsers()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable String id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(ApiResponse.success("User fetched successfully", user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody User user) {
        return ResponseEntity.ok(ApiResponse.success("User created successfully", userService.createUser(user)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable String id, @RequestBody User user) {
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", userService.updateUser(id, user)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    @GetMapping("/me/stats")
    public ResponseEntity<ApiResponse<UserStats>> getUserStats() {
        // Extract user ID from security context (set by JWT authentication filter)
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserStats stats = userService.getUserStats(userId);
        return ResponseEntity.ok(ApiResponse.success("User stats fetched successfully", stats));
    }
}
