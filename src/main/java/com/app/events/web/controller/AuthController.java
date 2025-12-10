package com.app.events.web.controller;

import com.app.events.dto.ApiResponse;
import com.app.events.model.User;
import com.app.events.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody User user) {
        String token = authService.login(user.getEmail(), user.getPassword());
        return ResponseEntity.ok(ApiResponse.success("Login successful", token));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<User>> signup(@RequestBody User user) {
        User created = authService.register(user);
        return ResponseEntity.ok(ApiResponse.success("User registered", created));
    }
}
