package com.hungpham.repository;

import com.hungpham.entity.FrontPageItemEntity;
import com.hungpham.entity.PostEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FrontPageItemRepository extends JpaRepository<FrontPageItemEntity, Long> {

    Optional<FrontPageItemEntity> findByPost(PostEntity post);


    // =========================
    // FEATURED (PINNED)
    // =========================

    /**
     * Lấy featured hiện tại (pinned = true)
     */
    Optional<FrontPageItemEntity> findFirstByPinnedTrueAndActiveTrue();

    /**
     * Clear featured (chỉ 1 bài được pinned)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update FrontPageItemEntity f " +
            "set f.pinned = false " +
            "where f.pinned = true")
    int clearPinned();

    // =========================
    // LIST ACTIVE ITEMS
    // =========================

    List<FrontPageItemEntity> findByActiveTrueOrderByPinnedDescPositionAsc();

    /**
     * Lấy items đang active + nằm trong time window
     */

    @Query("select f " +
            "from FrontPageItemEntity f " +
            "where f.active = true " +
            "and f.pinned = true " +
            "and (f.startAt is null or f.startAt <= :now) " +
            "and (f.endAt is null or f.endAt >= :now) " +
            "order by f.position asc")
    List<FrontPageItemEntity> findActiveFeatured(@Param("now") LocalDateTime now);


    // Lấy list items đang active + trong time window (dùng cho homepage)
    @Query("select f from FrontPageItemEntity f " +
            "where f.active = true " +
            "and (f.startAt is null or f.startAt <= :now) " +
            "and (f.endAt is null or f.endAt >= :now) " +
            "order by f.pinned desc, f.position asc")
    List<FrontPageItemEntity> findActiveFrontPageItems(@Param("now") LocalDateTime now);

    @Query("select f from FrontPageItemEntity f order by f.pinned desc, f.position asc, f.id asc")
    List<FrontPageItemEntity> findAllOrderByPinnedDescPositionAsc();
}
