package com.hungpham.service;


import com.hungpham.dtos.UserDto;
import com.hungpham.requests.auth.LoginRequest;
import com.hungpham.requests.auth.LogoutRequest;
import com.hungpham.requests.auth.RefreshTokenRequest;
import com.hungpham.response.auth.AuthTokenResponse;

public interface AuthService {
    AuthTokenResponse token(LoginRequest req, String ip, String userAgent);
    AuthTokenResponse refresh(RefreshTokenRequest req, String ip, String userAgent);
    void logout(LogoutRequest req);
    UserDto me(String userId);
}
