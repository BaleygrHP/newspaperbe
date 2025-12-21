package com.hungpham.entity;

import com.hungpham.common.enums.AuditActionEnum;
import com.hungpham.common.enums.AuditEntityTypeEnum;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "audit_logs")
public class AuditLogEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private UserEntity actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private AuditActionEnum action;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 20)
    private AuditEntityTypeEnum entityType;

    @Column(name = "entity_id", columnDefinition = "BINARY(16)")
    private byte[] entityId;

    @Column(name = "meta", columnDefinition = "JSON")
    private String meta;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
