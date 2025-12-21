package com.hungpham.common.enums;

public enum SectionEnum {

    EDITORIAL,
    NOTES,
    DIARY;

    public static SectionEnum from(String value) {
        for (SectionEnum s : values()) {
            if (s.name().equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Invalid section: " + value);
    }
}
