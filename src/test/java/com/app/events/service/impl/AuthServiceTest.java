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

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private com.app.events.config.JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId("usr-1");
        user.setEmail("test@example.com");
        user.setPassword("encoded_password");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(Collections.singletonList("USER"));
    }

    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);
        when(jwtUtil.generateToken("usr-1", "test@example.com", Collections.singletonList("USER")))
                .thenReturn("mock-token");
        when(jwtUtil.getExpirationDateFromToken("mock-token")).thenReturn(new java.util.Date());

        com.app.events.dto.LoginResponse response = authService.login("test@example.com", "password123");

        assertNotNull(response);
        assertEquals("mock-token", response.getToken());
        assertEquals("John", response.getUser().getFirstName());
        assertEquals("Doe", response.getUser().getLastName());
    }

    @Test
    void login_shouldThrowException_whenPasswordIsInvalid() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "encoded_password")).thenReturn(false);

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
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password_new");

        User newUser = new User();
        newUser.setEmail("test@example.com");
        newUser.setPassword("password123");

        User registered = authService.register(newUser);

        assertNotNull(registered);
        assertEquals("test@example.com", registered.getEmail());
        assertEquals("encoded_password_new", registered.getPassword());
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
