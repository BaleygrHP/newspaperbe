package com.hungpham.config.security;

import com.hungpham.common.enums.UserRoleEnum;

import java.io.Serializable;

public class AuthenticatedUser implements Serializable {
    private final String userId;
    private final String email;
    private final UserRoleEnum role;

    public AuthenticatedUser(String userId, String email, UserRoleEnum role) {
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public UserRoleEnum getRole() {
        return role;
    }
}
