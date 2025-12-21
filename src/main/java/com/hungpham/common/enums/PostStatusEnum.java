package com.hungpham.common.enums;

public enum PostStatusEnum {

    DRAFT,
    PUBLISHED,
    ARCHIVED;

    public static PostStatusEnum from(String value) {
        for (PostStatusEnum s : values()) {
            if (s.name().equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Invalid post status: " + value);
    }

    public boolean isPublished() {
        return this == PUBLISHED;
    }
}
