package com.app.events.service.impl;

import com.app.events.config.JwtUtil;
import com.app.events.dto.LoginResponse;
import com.app.events.model.User;
import com.app.events.repository.UserRepository;
import com.app.events.service.AuthService;
import com.app.events.service.EmailService;
import com.app.events.service.SequenceGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final EmailService emailService;

    @Override
    public LoginResponse login(String email, String password) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Invalid email"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtUtil.generateToken(user.getUserId(), user.getEmail(), user.getRole());
        Date expiresAt = jwtUtil.getExpirationDateFromToken(token);

        return new LoginResponse(token, expiresAt, LoginResponse.UserResponse.fromUser(user));
    }

    @Override
    public User register(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // Generate userId in format AB00001
        // Sync with existing users first
        userRepository.findTopByOrderByUserIdDesc().ifPresent(lastUser -> {
            if (Objects.nonNull(lastUser.getUserId()) && lastUser.getUserId().startsWith("AB")) {
                try {
                    long lastSeq = Long.parseLong(lastUser.getUserId().substring(2));
                    sequenceGeneratorService.syncSequence("user_sequence", lastSeq);
                } catch (NumberFormatException ignored) {
                }
            }
        });

        long sequence = sequenceGeneratorService.generateSequence("user_sequence");
        String userId = String.format("AB%05d", sequence);
        user.setUserId(userId);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(user.getRole() == null ? Collections.singletonList("USER") : user.getRole());
        User savedUser = userRepository.save(user);

        // Send welcome email
        if (savedUser.getEmail() != null) {
            emailService.sendSimpleMessage(savedUser.getEmail(), "Welcome to Event Planner", "Hello " + savedUser.getFirstName() + ",\n\nWelcome to our platform! Your User ID is " + savedUser.getUserId());
        }

        return savedUser;
    }
}
