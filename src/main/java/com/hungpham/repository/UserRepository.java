package com.hungpham.repository;

import com.hungpham.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, byte[]> {

    Optional<UserEntity> findByEmailAndIsActiveTrue(String email);

    boolean existsByEmail(String email);
    Optional<UserEntity> findById(byte[] id);
}
