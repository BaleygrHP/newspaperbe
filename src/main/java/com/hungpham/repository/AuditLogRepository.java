package com.hungpham.repository;

import com.hungpham.entity.AuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {
    Page<AuditLogEntity> findByActor_IdOrderByCreatedDateDesc(
            byte[] actorId,
            Pageable pageable
    );

}