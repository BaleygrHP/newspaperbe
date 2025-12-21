package com.hungpham.common.enums;

public enum PostMediaRoleEnum {

    COVER,
    CONTENT;

    public static PostMediaRoleEnum from(String value) {
        for (PostMediaRoleEnum r : values()) {
            if (r.name().equalsIgnoreCase(value)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Invalid post media role: " + value);
    }
}
