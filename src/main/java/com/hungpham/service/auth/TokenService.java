package com.hungpham.service.auth;

import com.hungpham.entity.UserEntity;
import com.hungpham.mappers.UuidBinaryMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
public class TokenService {

    private final UuidBinaryMapper uuidBinaryMapper;

    @Value("${app.auth.jwt-secret}")
    private String jwtSecret;

    @Value("${app.auth.access-ttl-seconds:900}")
    private long accessTtlSeconds;

    private Key signingKey;

    public TokenService(UuidBinaryMapper uuidBinaryMapper) {
        this.uuidBinaryMapper = uuidBinaryMapper;
    }

    @PostConstruct
    public void init() {
        byte[] secretBytes = jwtSecret == null ? new byte[0] : jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("app.auth.jwt-secret must be at least 32 bytes");
        }
        this.signingKey = Keys.hmacShaKeyFor(secretBytes);
    }

    public String createAccessToken(UserEntity user) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessTtlSeconds * 1000L);
        String userId = uuidBinaryMapper.toUuid(user.getId());

        return Jwts.builder()
                .setSubject(userId)
                .claim("role", user.getRole() == null ? null : user.getRole().name())
                .claim("email", user.getEmail())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUserId(String token) {
        try {
            Jws<Claims> parsed = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);
            return parsed.getBody().getSubject();
        } catch (Exception ex) {
            throw new BadCredentialsException("Invalid access token");
        }
    }

    public long getAccessTtlSeconds() {
        return accessTtlSeconds;
    }
}
