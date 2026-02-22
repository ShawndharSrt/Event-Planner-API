package com.app.events.web.controller.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResetPasswordRequest {

    private String token;
    private String newPassword;
}
