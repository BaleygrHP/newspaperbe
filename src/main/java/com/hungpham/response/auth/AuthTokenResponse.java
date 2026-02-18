package com.hungpham.response.auth;

import com.hungpham.dtos.UserDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthTokenResponse {
    private String tokenType;
    private String accessToken;
    private long accessTokenExpiresIn;
    private String refreshToken;
    private long refreshTokenExpiresIn;
    private UserDto user;
}
