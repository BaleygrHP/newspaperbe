package com.hungpham.repository;

import com.hungpham.common.enums.MediaKindEnum;
import com.hungpham.entity.MediaAssetEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

@Repository
public interface MediaAssetRepository extends JpaRepository<MediaAssetEntity, byte[]> {
    boolean existsByFileHash(String fileHash);

    List<MediaAssetEntity> findByOwner_IdOrderByCreatedDateDesc(byte[] ownerId);

    // ===== Public Gallery =====
    Page<MediaAssetEntity> findByActiveTrueAndKind(MediaKindEnum kind, Pageable pageable);

    Page<MediaAssetEntity> findByActiveTrue(Pageable pageable);

    @Query("select m " +
            "from MediaAssetEntity m " +
            "where (:kind is null or m.kind = :kind) " +
            "and (:active is null or m.active = :active) " +
            "and (:category is null or m.category = :category) " +
            "and ( " +
            "  :q is null " +
            "  or lower(m.title) like lower(concat('%', :q, '%')) " +
            "  or lower(m.alt) like lower(concat('%', :q, '%')) " +
            "  or lower(m.caption) like lower(concat('%', :q, '%')) " +
            "  or lower(m.location) like lower(concat('%', :q, '%')) " +
            "  or lower(m.url) like lower(concat('%', :q, '%')) " +
            ")")
    Page<MediaAssetEntity> adminSearch(
            @Param("kind") MediaKindEnum kind,
            @Param("active") Boolean active,
            @Param("category") String category,
            @Param("q") String q,
            Pageable pageable
    );

    @Query("select distinct m.category " +
            "from MediaAssetEntity m " +
            "where m.category is not null " +
            "and m.category <> '' " +
            "order by m.category asc")
    List<String> listDistinctCategories();


}
