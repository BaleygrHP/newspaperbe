package com.hungpham.common.enums;

public enum MediaKindEnum {

    IMAGE,
    VIDEO,
    FILE;

    public static MediaKindEnum from(String value) {
        for (MediaKindEnum k : values()) {
            if (k.name().equalsIgnoreCase(value)) {
                return k;
            }
        }
        throw new IllegalArgumentException("Invalid media kind: " + value);
    }
}
