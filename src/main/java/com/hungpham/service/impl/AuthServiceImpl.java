package com.hungpham.service.impl;

import com.hungpham.common.exception.BadRequestException;
import com.hungpham.common.exception.EntityNotFoundException;
import com.hungpham.dtos.UserDto;
import com.hungpham.entity.UserEntity;
import com.hungpham.mappers.UserMapper;
import com.hungpham.mappers.UuidBinaryMapper;
import com.hungpham.repository.UserRepository;
import com.hungpham.requests.auth.LoginRequest;
import com.hungpham.requests.auth.LogoutRequest;
import com.hungpham.requests.auth.RefreshTokenRequest;
import com.hungpham.response.auth.AuthTokenResponse;
import com.hungpham.service.AuthService;
import com.hungpham.service.auth.RefreshTokenService;
import com.hungpham.service.auth.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UuidBinaryMapper uuidBinaryMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;

    public AuthServiceImpl(UserRepository userRepository,
                           UserMapper userMapper,
                           UuidBinaryMapper uuidBinaryMapper,
                           PasswordEncoder passwordEncoder,
                           TokenService tokenService,
                           RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.uuidBinaryMapper = uuidBinaryMapper;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    @Transactional
    public AuthTokenResponse token(LoginRequest req, String ip, String userAgent) {
        if (req == null) throw new BadRequestException("Body is required");
        if (isEmpty(req.getEmail())) throw new BadRequestException("email is required");
        if (isEmpty(req.getPassword())) throw new BadRequestException("password is required");

        String email = req.getEmail().trim().toLowerCase();
        UserEntity user = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!matchesPassword(req.getPassword(), user.getPasswordHash())) {
            log.warn("[Auth][Token] invalid credentials email={}", email);
            throw new BadCredentialsException("Invalid email or password");
        }

        RefreshTokenService.RefreshIssue refreshIssue = refreshTokenService.issueForUser(user, ip, userAgent);
        String accessToken = tokenService.createAccessToken(user);

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return buildTokenResponse(user, accessToken, refreshIssue.getRefreshToken());
    }

    @Override
    @Transactional
    public AuthTokenResponse refresh(RefreshTokenRequest req, String ip, String userAgent) {
        if (req == null || isEmpty(req.getRefreshToken())) {
            throw new BadCredentialsException("Refresh token is required");
        }

        RefreshTokenService.RefreshIssue refreshIssue =
                refreshTokenService.rotate(req.getRefreshToken().trim(), ip, userAgent);

        UserEntity user = refreshIssue.getUser();
        if (!user.isActive()) {
            refreshTokenService.revokeByToken(refreshIssue.getRefreshToken(), "INACTIVE_USER");
            throw new BadCredentialsException("User is inactive");
        }

        String accessToken = tokenService.createAccessToken(user);
        return buildTokenResponse(user, accessToken, refreshIssue.getRefreshToken());
    }

    @Override
    @Transactional
    public void logout(LogoutRequest req) {
        if (req == null || isEmpty(req.getRefreshToken())) {
            return;
        }
        refreshTokenService.revokeByToken(req.getRefreshToken().trim(), "LOGOUT");
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto me(String userId) {
        if (isEmpty(userId)) {
            throw new BadRequestException("Missing authenticated user");
        }

        byte[] id = uuidBinaryMapper.toBytes(userId.trim());
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        if (!user.isActive()) {
            throw new BadRequestException("User is inactive");
        }

        return userMapper.toDto(user);
    }

    private AuthTokenResponse buildTokenResponse(UserEntity user, String accessToken, String refreshToken) {
        AuthTokenResponse response = new AuthTokenResponse();
        response.setTokenType("Bearer");
        response.setAccessToken(accessToken);
        response.setAccessTokenExpiresIn(tokenService.getAccessTtlSeconds());
        response.setRefreshToken(refreshToken);
        response.setRefreshTokenExpiresIn(refreshTokenService.getRefreshTtlSeconds());
        response.setUser(userMapper.toDto(user));
        return response;
    }

    private boolean matchesPassword(String rawPassword, String storedHash) {
        if (storedHash == null) return false;
        if (storedHash.startsWith("$2")) {
            return passwordEncoder.matches(rawPassword, storedHash);
        }
        return storedHash.equals(rawPassword);
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
