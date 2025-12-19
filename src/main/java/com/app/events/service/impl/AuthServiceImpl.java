package com.app.events.service.impl;

import com.app.events.model.User;
import com.app.events.repository.UserRepository;
import com.app.events.service.AuthService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Collections;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final com.app.events.config.JwtUtil jwtUtil;
    private final com.app.events.service.SequenceGeneratorService sequenceGeneratorService;
    private final com.app.events.service.EmailService emailService;

    // Remove local constants SECRET_KEY and EXPIRATION_TIME as we use JwtUtil now

    @Override
    public com.app.events.dto.LoginResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getUserId(), user.getEmail(), user.getRole());
        java.util.Date expiresAt = jwtUtil.getExpirationDateFromToken(token);

        return new com.app.events.dto.LoginResponse(token, expiresAt,
                com.app.events.dto.LoginResponse.UserResponse.fromUser(user));
    }

    @Override
    public User register(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // Generate userId in format AB00001
        // Sync with existing users first
            userRepository.findTopByOrderByUserIdDesc().ifPresent(lastUser -> {
            if (lastUser.getUserId() != null && lastUser.getUserId().startsWith("AB")) {
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
            emailService.sendSimpleMessage(
                    savedUser.getEmail(),
                    "Welcome to Event Planner",
                    "Hello " + savedUser.getFirstName() + ",\n\nWelcome to our platform! Your User ID is "
                            + savedUser.getUserId());
        }

        return savedUser;
    }
}
