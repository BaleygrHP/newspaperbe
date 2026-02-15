package com.hungpham.controller;

import com.hungpham.dtos.UserDto;
import com.hungpham.requests.auth.LoginRequest;
import com.hungpham.response.auth.LoginResponse;
import com.hungpham.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @GetMapping("/me")
    public UserDto me(@RequestHeader(value = "X-Actor-UserId", required = false) String actorUserId) {
        return authService.me(actorUserId);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Stateless auth – cookie clearing is done on the frontend side
        return ResponseEntity.ok(Collections.singletonMap("ok", true));
    }
}
