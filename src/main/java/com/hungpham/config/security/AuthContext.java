package com.hungpham.config.security;

import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthContext {

    public String requireUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new InsufficientAuthenticationException("Authentication required");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof AuthenticatedUser)) {
            throw new InsufficientAuthenticationException("Authentication required");
        }

        String userId = ((AuthenticatedUser) principal).getUserId();
        if (userId == null || userId.trim().isEmpty()) {
            throw new InsufficientAuthenticationException("Authentication required");
        }
        return userId;
    }
}
