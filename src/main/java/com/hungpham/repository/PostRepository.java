package com.hungpham.repository;

import com.hungpham.common.enums.PostStatusEnum;
import com.hungpham.entity.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, byte[]> {
    //Lấy bài theo slug
    Optional<PostEntity> findBySlug(String slug);

    //Lấy bài public theo slug
    Optional<PostEntity> findBySlugAndStatusAndDeletedAtIsNull(
            String slug,
            PostStatusEnum status
    );

    //List bài theo section (Public)
    Page<PostEntity> findBySection_KeyAndStatusAndDeletedAtIsNull(
            String sectionKey,
            PostStatusEnum status,
            Pageable pageable
    );

    // Bài mới nhất (Front Page – Latest)
    Page<PostEntity> findByStatusAndDeletedAtIsNull(
            PostStatusEnum status,
            Pageable pageable
    );

    // Bài liên quan (Related)
    @Query("select p from PostEntity p " +
            "where p.section.key = :sectionKey " +
            "and p.id <> :postId " +
            "and p.status = :status " +
            "and p.deletedAt is null " +
            "order by p.publishedAt desc")
    List<PostEntity> findRelatedPosts(@Param("sectionKey") String sectionKey,
                                      @Param("postId") byte[] postId,
                                      @Param("status") PostStatusEnum status,
                                      Pageable pageable);
    // Kiểm tra slug trùng
    boolean existsBySlug(String slug);


    // =============================
    default List<PostEntity> findRelatedPosts(String sectionKey, byte[] postId, Pageable pageable) {
        return findRelatedPosts(sectionKey, postId, PostStatusEnum.PUBLISHED, pageable);
    }

    @Query("select p from PostEntity p " +
            "where p.status = :status " +
            "and p.deletedAt is null " +
            "and p.publishedAt >= :from " +
            "and p.publishedAt < :to " +
            "order by p.publishedAt desc")
    Page<PostEntity> findArchivePublished(@Param("status") PostStatusEnum status,
                                          @Param("from") LocalDateTime from,
                                          @Param("to") LocalDateTime to,
                                          Pageable pageable);

    default Page<PostEntity> findArchivePublished(LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return findArchivePublished(PostStatusEnum.PUBLISHED, from, to, pageable);
    }

    @Query("select p from PostEntity p " +
            "where p.section.key = :sectionKey " +
            "and p.status = :status " +
            "and p.deletedAt is null " +
            "and p.publishedAt >= :from " +
            "and p.publishedAt < :to " +
            "order by p.publishedAt desc")
    Page<PostEntity> findArchivePublishedBySection(@Param("sectionKey") String sectionKey,
                                                   @Param("status") PostStatusEnum status,
                                                   @Param("from") LocalDateTime from,
                                                   @Param("to") LocalDateTime to,
                                                   Pageable pageable);

    default Page<PostEntity> findArchivePublishedBySection(String sectionKey, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return findArchivePublishedBySection(sectionKey, PostStatusEnum.PUBLISHED, from, to, pageable);
    }
    // =========
    // Admin search (status/section/q) + soft delete
    // =========
    @Query("select p from PostEntity p " +
            "where (:status is null or p.status = :status) " +
            "and (:sectionId is null or p.section.id = :sectionId) " +
            "and (:q is null or lower(p.title) like lower(concat('%', :q, '%')) " +
            "     or lower(p.slug) like lower(concat('%', :q, '%'))) " +
            "and p.deletedAt is null")
    Page<PostEntity> adminSearch(@Param("status") PostStatusEnum status,
                                 @Param("sectionId") byte[] sectionId,
                                 @Param("q") String q,
                                 Pageable pageable);


    // Optional: lấy list showOnFrontPage (nếu bạn muốn build block theo cờ thay vì section)
    Page<PostEntity> findByShowOnFrontPageTrueAndStatusAndDeletedAtIsNull(PostStatusEnum status, Pageable pageable);



}
