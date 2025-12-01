package com.app.events.service.impl;

import com.app.events.model.User;
import com.app.events.repository.UserRepository;
import com.app.events.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

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
        // TODO: Encode password
        return userRepository.save(user);
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
