package com.ubaid.Auth_service.security;

import com.ubaid.Auth_service.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUserUtil {

    private SecurityUserUtil() {}

    public static Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("Unauthenticated");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof User u) {
            return u.getId();
        }
        throw new IllegalStateException("Unexpected principal type: " + principal.getClass());
    }

    public static String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("Unauthenticated");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof User u) {
            return u.getUsername();
        }
        return auth.getName();
    }
}
