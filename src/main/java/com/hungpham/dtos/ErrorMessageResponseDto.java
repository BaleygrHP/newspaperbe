package com.hungpham.dtos;
import com.hungpham.common.enums.ErrorCodeEnum;

public class ErrorMessageResponseDto {
    private ErrorCodeEnum errorCode;
    private String message;
    private long timestamp;
    public ErrorMessageResponseDto(ErrorCodeEnum errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public ErrorCodeEnum getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCodeEnum errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
