package com.hungpham.common.enums;

public enum ErrorCodeEnum {
    INTERNAL_SERVER_ERROR,
    AUTHENTICATION_FAILED,
    AUTHORIZATION_FAILED,
    ENTITY_NOT_FOUND,
    PAGE_NOT_FOUND,
    METHOD_NOT_SUPPORTED,
    CONTENT_TYPE_NOT_SUPPORTED,
    PARAM_MISSING,
    PARAM_INVALID,
    REQUEST_NOT_READABLE,
    SERVICE_UNAVAILABLE,
    BAD_REQUEST,
    TOO_MANY_REQUESTS,
    ILLEGAL_ARGUMENTS,
    UNPROCESSABLE_ENTITY,
    IO_ERROR,
    FORBIDDEN,
    CONFLICT;
    public static ErrorCodeEnum from(String value) {
        for (ErrorCodeEnum s : values()) {
            if (s.name().equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Invalid error code: " + value);
    }

}
