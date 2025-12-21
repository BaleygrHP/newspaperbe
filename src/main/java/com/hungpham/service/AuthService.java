package com.hungpham.service;


import com.hungpham.dtos.UserDto;
import com.hungpham.requests.auth.LoginRequest;
import com.hungpham.response.auth.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest req);
    UserDto me(String userId);
}
