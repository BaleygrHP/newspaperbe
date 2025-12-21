package com.hungpham.common.enums;

public enum AuditEntityTypeEnum {

    POST,
    MEDIA,
    USER,
    SYSTEM;

    public static AuditEntityTypeEnum from(String value) {
        for (AuditEntityTypeEnum e : values()) {
            if (e.name().equalsIgnoreCase(value)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid audit entity type: " + value);
    }
}
