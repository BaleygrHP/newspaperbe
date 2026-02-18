package com.hungpham.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "auth_refresh_sessions")
public class AuthRefreshSessionEntity {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "BINARY(16)")
    private byte[] id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "family_id", nullable = false, columnDefinition = "BINARY(16)")
    private byte[] familyId;

    @Column(name = "token_hash", nullable = false, length = 64, unique = true)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_ip", length = 64)
    private String createdIp;

    @Column(name = "created_user_agent", length = 255)
    private String createdUserAgent;

    @Column(name = "rotated_at")
    private LocalDateTime rotatedAt;

    @Column(name = "replaced_by_hash", length = 64)
    private String replacedByHash;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "revoked_reason", length = 100)
    private String revokedReason;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
}
