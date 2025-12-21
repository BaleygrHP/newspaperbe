package com.hungpham.response.auth;


import com.hungpham.dtos.UserDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    private UserDto user;
    // Phase 1: chưa bắt buộc JWT
    // private String accessToken;
}
