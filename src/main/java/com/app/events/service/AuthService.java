package com.app.events.service;

import com.app.events.dto.LoginResponse;
import com.app.events.model.User;
import com.app.events.web.controller.model.ResetPasswordRequest;

public interface AuthService {
    LoginResponse login(String email, String password);

    User register(User user);

    void forgotPassword(String email);

    LoginResponse resetPassword(ResetPasswordRequest request);
}
