package com.app.events.service.impl;

import com.app.events.model.User;
import com.app.events.repository.UserRepository;
import com.app.events.service.SequenceGeneratorService;
import com.app.events.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final com.app.events.service.EmailService emailService;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User createUser(User user) {
        // Generate userId in format AB00001, AB00002, etc.
        long sequence = sequenceGeneratorService.generateSequence("user_sequence");
        String userId = String.format("AB%05d", sequence);
        user.setUserId(userId);

        // TODO: Encode password
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

    @Override
    public User updateUser(String id, User user) {
        if (userRepository.existsById(id)) {
            user.setId(id);
            // TODO: Handle password update securely
            return userRepository.save(user);
        }
        throw new RuntimeException("User not found with id: " + id);
    }

    @Override
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }
}
