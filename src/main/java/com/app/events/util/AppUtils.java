package com.app.events.util;

import org.springframework.security.core.context.SecurityContextHolder;

public class AppUtils {

    public static String getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof String) {
            return (String) principal;
        }
        return principal.toString();
    }
}
