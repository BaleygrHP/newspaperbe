package com.hungpham.repository;

import com.hungpham.entity.PostMediaEntity;
import com.hungpham.entity.PostMediaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostMediaRepository extends JpaRepository<PostMediaEntity, PostMediaId> {
    List<PostMediaEntity> findByPost_Id(byte[] postId);

    void deleteByPost_Id(byte[] postId);
}