package com.hungpham.repository;

import com.hungpham.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByUserName(String userName);
    Optional<UserEntity> findByUserNameAndPassword(String userName, String password);
    int countByUserName(String userName);
}
