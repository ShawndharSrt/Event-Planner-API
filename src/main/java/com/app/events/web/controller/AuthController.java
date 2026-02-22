package com.app.events.web.controller;

import com.app.events.dto.ApiResponse;
import com.app.events.dto.LoginResponse;
import com.app.events.model.User;
import com.app.events.service.AuthService;
import com.app.events.web.controller.model.ResetPasswordRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Object>> login(@RequestBody User user) {
        LoginResponse loginResponse = authService.login(user.getEmail(), user.getPassword());
        return ResponseEntity.ok(ApiResponse.success("Login successful", loginResponse));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<User>> signup(@RequestBody User user) {
        User created = authService.register(user);
        return ResponseEntity.ok(ApiResponse.success("User registered", created));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam(name = "email") String email) {
        authService.forgotPassword(email);
        return ResponseEntity.ok("If email exists, reset link sent");
    }

    @PostMapping("/reset-password")
    public LoginResponse resetPassword(@RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }
}
