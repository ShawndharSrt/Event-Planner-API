package com.app.events.service.impl;

import com.app.events.model.User;
import com.app.events.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId("usr-1");
        user.setEmail("test@example.com");
        user.setPassword(encoder.encode("password123")); // Store encoded password
        user.setRole(Collections.singletonList("USER"));
    }

    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        String token = authService.login("test@example.com", "password123");

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void login_shouldThrowException_whenPasswordIsInvalid() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> authService.login("test@example.com", "wrongpassword"));
    }

    @Test
    void login_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.login("unknown@example.com", "password123"));
    }

    @Test
    void register_shouldSaveUser() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User newUser = new User();
        newUser.setEmail("test@example.com");
        newUser.setPassword("password123");

        User registered = authService.register(newUser);

        assertNotNull(registered);
        assertEquals("test@example.com", registered.getEmail());
        assertNotEquals("password123", registered.getPassword()); // Should be encoded
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrowException_whenEmailExists() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        User newUser = new User();
        newUser.setEmail("test@example.com");

        assertThrows(RuntimeException.class, () -> authService.register(newUser));
    }
}
