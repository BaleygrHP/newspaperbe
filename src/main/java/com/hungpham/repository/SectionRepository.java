package com.hungpham.repository;

import com.hungpham.common.enums.SectionVisibilityEnum;
import com.hungpham.entity.SectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SectionRepository extends JpaRepository<SectionEntity, byte[]> {

    // Public: lấy section theo key (editorial / notes / diary)
    Optional<SectionEntity> findByKeyAndActiveTrue(String key);
    // Public menu
    List<SectionEntity> findByActiveTrueAndVisibilityOrderBySortOrderAsc(
            SectionVisibilityEnum visibility
    );
    // Admin: check duplicate key
    boolean existsByKey(String key);
    List<SectionEntity> findByActiveTrueOrderBySortOrderAsc();
    Optional<SectionEntity> findById(byte[] id);

    List<SectionEntity> findAllByOrderBySortOrderAsc();

    Optional<SectionEntity> findByKey(String key);
}
