package com.hungpham.repository;

import com.hungpham.entity.AuthRefreshSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuthRefreshSessionRepository extends JpaRepository<AuthRefreshSessionEntity, byte[]> {
    Optional<AuthRefreshSessionEntity> findByTokenHash(String tokenHash);
    List<AuthRefreshSessionEntity> findByFamilyId(byte[] familyId);
}
