package com.hungpham.service.auth;

import com.hungpham.entity.AuthRefreshSessionEntity;
import com.hungpham.entity.UserEntity;
import com.hungpham.mappers.UuidBinaryMapper;
import com.hungpham.repository.AuthRefreshSessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
public class RefreshTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final AuthRefreshSessionRepository refreshSessionRepository;
    private final UuidBinaryMapper uuidBinaryMapper;

    @Value("${app.auth.refresh-ttl-seconds:604800}")
    private long refreshTtlSeconds;

    public RefreshTokenService(AuthRefreshSessionRepository refreshSessionRepository,
                               UuidBinaryMapper uuidBinaryMapper) {
        this.refreshSessionRepository = refreshSessionRepository;
        this.uuidBinaryMapper = uuidBinaryMapper;
    }

    @Transactional
    public RefreshIssue issueForUser(UserEntity user, String ip, String userAgent) {
        byte[] familyId = uuidBinaryMapper.newUuidBytes();
        return createSession(user, familyId, ip, userAgent);
    }

    @Transactional
    public RefreshIssue rotate(String presentedToken, String ip, String userAgent) {
        if (presentedToken == null || presentedToken.trim().isEmpty()) {
            throw new BadCredentialsException("Refresh token is required");
        }

        LocalDateTime now = LocalDateTime.now();
        String hash = hashToken(presentedToken);

        AuthRefreshSessionEntity current = refreshSessionRepository.findByTokenHash(hash)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (current.getRevokedAt() != null || current.getRotatedAt() != null) {
            revokeFamily(current.getFamilyId(), now, "REUSE_DETECTED");
            throw new BadCredentialsException("Refresh token reuse detected");
        }

        if (current.getExpiresAt() != null && current.getExpiresAt().isBefore(now)) {
            current.setRevokedAt(now);
            current.setRevokedReason("EXPIRED");
            current.setLastUsedAt(now);
            refreshSessionRepository.save(current);
            throw new BadCredentialsException("Refresh token expired");
        }

        RefreshIssue next = createSession(current.getUser(), current.getFamilyId(), ip, userAgent);

        current.setRotatedAt(now);
        current.setRevokedAt(now);
        current.setRevokedReason("ROTATED");
        current.setReplacedByHash(next.getRefreshTokenHash());
        current.setLastUsedAt(now);
        refreshSessionRepository.save(current);

        return next;
    }

    @Transactional
    public void revokeByToken(String presentedToken, String reason) {
        if (presentedToken == null || presentedToken.trim().isEmpty()) {
            return;
        }

        String hash = hashToken(presentedToken);
        AuthRefreshSessionEntity session = refreshSessionRepository.findByTokenHash(hash).orElse(null);
        if (session == null) return;

        revokeFamily(session.getFamilyId(), LocalDateTime.now(), reason == null ? "LOGOUT" : reason);
    }

    public long getRefreshTtlSeconds() {
        return refreshTtlSeconds;
    }

    private RefreshIssue createSession(UserEntity user, byte[] familyId, String ip, String userAgent) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime exp = now.plusSeconds(refreshTtlSeconds);

        String rawToken = generateToken();
        String hash = hashToken(rawToken);

        AuthRefreshSessionEntity session = new AuthRefreshSessionEntity();
        session.setId(uuidBinaryMapper.newUuidBytes());
        session.setUser(user);
        session.setFamilyId(familyId);
        session.setTokenHash(hash);
        session.setExpiresAt(exp);
        session.setCreatedAt(now);
        session.setCreatedIp(trimToNull(ip, 64));
        session.setCreatedUserAgent(trimToNull(userAgent, 255));
        session.setLastUsedAt(now);
        refreshSessionRepository.save(session);

        return new RefreshIssue(user, rawToken, hash, exp);
    }

    private void revokeFamily(byte[] familyId, LocalDateTime now, String reason) {
        List<AuthRefreshSessionEntity> sessions = refreshSessionRepository.findByFamilyId(familyId);
        for (AuthRefreshSessionEntity item : sessions) {
            if (item.getRevokedAt() == null) {
                item.setRevokedAt(now);
                item.setRevokedReason(reason);
                item.setLastUsedAt(now);
            }
        }
        refreshSessionRepository.saveAll(sessions);
    }

    private String trimToNull(String value, int maxLen) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return null;
        return trimmed.length() <= maxLen ? trimmed : trimmed.substring(0, maxLen);
    }

    private String generateToken() {
        byte[] data = new byte[48];
        RANDOM.nextBytes(data);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    public static class RefreshIssue {
        private final UserEntity user;
        private final String refreshToken;
        private final String refreshTokenHash;
        private final LocalDateTime refreshExpiresAt;

        public RefreshIssue(UserEntity user, String refreshToken, String refreshTokenHash, LocalDateTime refreshExpiresAt) {
            this.user = user;
            this.refreshToken = refreshToken;
            this.refreshTokenHash = refreshTokenHash;
            this.refreshExpiresAt = refreshExpiresAt;
        }

        public UserEntity getUser() {
            return user;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public String getRefreshTokenHash() {
            return refreshTokenHash;
        }

        public LocalDateTime getRefreshExpiresAt() {
            return refreshExpiresAt;
        }
    }
}
