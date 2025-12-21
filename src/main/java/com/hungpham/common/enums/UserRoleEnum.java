package com.hungpham.common.enums;

public enum UserRoleEnum {

    ADMIN,
    EDITOR;

    public static UserRoleEnum from(String value) {
        for (UserRoleEnum r : values()) {
            if (r.name().equalsIgnoreCase(value)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Invalid user role: " + value);
    }
}
