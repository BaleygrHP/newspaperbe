package com.hungpham.common.enums;

public enum AuditActionEnum {

    CREATE,
    UPDATE,
    DELETE,
    PUBLISH,
    UNPUBLISH,
    ROLLBACK,
    UPLOAD,
    LOGIN,
    LOGOUT;

    public static AuditActionEnum from(String value) {
        for (AuditActionEnum a : values()) {
            if (a.name().equalsIgnoreCase(value)) {
                return a;
            }
        }
        throw new IllegalArgumentException("Invalid audit action: " + value);
    }
}
