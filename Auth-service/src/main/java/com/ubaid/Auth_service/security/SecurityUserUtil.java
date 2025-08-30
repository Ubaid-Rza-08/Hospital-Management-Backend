package com.ubaid.Auth_service.security;

import com.ubaid.Auth_service.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class SecurityUserUtil {

    private SecurityUserUtil() {}

    public static Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("Unauthenticated");
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof User user) {
            return user.getId();
        }

        // Fallback to details if available
        if (auth.getDetails() instanceof Map details) {
            Object userId = details.get("userId");
            if (userId instanceof Long) {
                return (Long) userId;
            }
        }

        throw new IllegalStateException("Unable to determine user ID");
    }

    public static String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("Unauthenticated");
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof User user) {
            return user.getUsername();
        }

        // Fallback to details if available
        if (auth.getDetails() instanceof Map details) {
            Object username = details.get("username");
            if (username instanceof String) {
                return (String) username;
            }
        }

        return auth.getName();
    }

    public static Set<String> currentUserRoles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new IllegalStateException("Unauthenticated");
        }

        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring(5)) // Remove "ROLE_" prefix
                .collect(Collectors.toSet());
    }

    public static boolean hasRole(String role) {
        return currentUserRoles().contains(role);
    }

    public static boolean hasAnyRole(String... roles) {
        Set<String> userRoles = currentUserRoles();
        return Arrays.stream(roles).anyMatch(userRoles::contains);
    }
}
