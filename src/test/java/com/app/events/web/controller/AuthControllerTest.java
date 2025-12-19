package com.app.events.web.controller;

import com.app.events.config.MongoConfig;
import com.app.events.dto.ApiResponse;
import com.app.events.model.User;
import com.app.events.service.AuthService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AuthController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
}, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MongoConfig.class))
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId("usr-1");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setRole(Collections.singletonList("USER"));
    }

    @Test
    void login_shouldReturnToken() throws Exception {
        com.app.events.dto.LoginResponse mockResponse = new com.app.events.dto.LoginResponse(
                "mock-jwt-token",
                new java.util.Date(),
                new com.app.events.dto.LoginResponse.UserResponse("usr-1", "John", "Doe", "test@example.com",
                        Collections.singletonList("USER")));
        when(authService.login("test@example.com", "password123")).thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.data.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.user.firstName").value("John"))
                .andExpect(jsonPath("$.data.user.lastName").value("Doe"))
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    void signup_shouldReturnRegisteredUser() throws Exception {
        when(authService.register(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("usr-1"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }
}
