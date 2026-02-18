package com.hungpham.controller;

import com.hungpham.config.security.AuthContext;
import com.hungpham.dtos.UserDto;
import com.hungpham.requests.auth.LoginRequest;
import com.hungpham.requests.auth.LogoutRequest;
import com.hungpham.requests.auth.RefreshTokenRequest;
import com.hungpham.response.auth.AuthTokenResponse;
import com.hungpham.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthContext authContext;

    public AuthController(AuthService authService, AuthContext authContext) {
        this.authService = authService;
        this.authContext = authContext;
    }

    @PostMapping("/token")
    public AuthTokenResponse token(@RequestBody LoginRequest req, HttpServletRequest request) {
        return authService.token(req, clientIp(request), userAgent(request));
    }

    @PostMapping("/refresh")
    public AuthTokenResponse refresh(@RequestBody RefreshTokenRequest req, HttpServletRequest request) {
        return authService.refresh(req, clientIp(request), userAgent(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody(required = false) LogoutRequest req) {
        authService.logout(req);
        return ResponseEntity.ok(Collections.singletonMap("ok", true));
    }

    @GetMapping("/me")
    public UserDto me() {
        return authService.me(authContext.requireUserId());
    }

    private String clientIp(HttpServletRequest request) {
        if (request == null) return null;
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.trim().isEmpty()) {
            int idx = forwarded.indexOf(',');
            return idx > -1 ? forwarded.substring(0, idx).trim() : forwarded.trim();
        }
        return request.getRemoteAddr();
    }

    private String userAgent(HttpServletRequest request) {
        if (request == null) return null;
        return request.getHeader("User-Agent");
    }
}
