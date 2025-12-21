package com.hungpham.repository;

import com.hungpham.entity.PostRevisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRevisionRepository extends JpaRepository<PostRevisionEntity, byte[]> {

    // Lấy revisionNo lớn nhất của 1 post
    @Query("select coalesce(max(r.revisionNo), 0) " +
            "from PostRevisionEntity r " +
            "where r.post.id = :postId")
    int getMaxRevisionNo(@Param("postId") byte[] postId);

    // Lịch sử revision (Admin)
    List<PostRevisionEntity> findByPost_IdOrderByRevisionNoDesc(byte[] postId);
}
