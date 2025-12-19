package com.app.events.dto;

import com.app.events.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private Date expiresAt;
    private UserResponse user;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserResponse {
        private String id;
        private String firstName;
        private String lastName;
        private String email;
        private Object roles;

        public static UserResponse fromUser(User user) {
            return new UserResponse(user.getUserId(), user.getFirstName(), user.getLastName(), user.getEmail(),
                    user.getRole());
        }
    }
}
