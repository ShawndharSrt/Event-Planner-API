package com.app.events.service;

import com.app.events.dto.LoginResponse;
import com.app.events.model.User;

public interface AuthService {
    LoginResponse login(String email, String password);

    User register(User user);
}
