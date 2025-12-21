package com.hungpham.common.enums;

public enum SectionVisibilityEnum {

    PUBLIC,
    PRIVATE;

    public static SectionVisibilityEnum from(String value) {
        for (SectionVisibilityEnum v : values()) {
            if (v.name().equalsIgnoreCase(value)) {
                return v;
            }
        }
        throw new IllegalArgumentException("Invalid section visibility: " + value);
    }
}
