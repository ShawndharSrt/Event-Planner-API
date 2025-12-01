package com.app.events.service;

import com.app.events.model.User;

public interface AuthService {
    String login(String email, String password);
    User register(User user);
}

