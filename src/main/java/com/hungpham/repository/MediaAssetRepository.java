package com.hungpham.repository;

import com.hungpham.entity.MediaAssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaAssetRepository extends JpaRepository<MediaAssetEntity, byte[]> {
    boolean existsByFileHash(String fileHash);

    List<MediaAssetEntity> findByOwner_IdOrderByCreatedAtDesc(byte[] ownerId);

}
