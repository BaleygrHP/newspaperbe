package com.hungpham.common.enums;

public enum MediaStorageEnum {

    URL,
    S3,
    LOCAL,
    CDN;

    public static MediaStorageEnum from(String value) {
        for (MediaStorageEnum s : values()) {
            if (s.name().equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Invalid media storage: " + value);
    }
}
