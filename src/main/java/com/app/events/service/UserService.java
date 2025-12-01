package com.app.events.service;

import com.app.events.model.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> getAllUsers();

    Optional<User> getUserById(String id);

    Optional<User> getUserByEmail(String email);

    User createUser(User user);

    User updateUser(String id, User user);

    void deleteUser(String id);
}
