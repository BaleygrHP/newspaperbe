package com.hungpham.repository;


import com.hungpham.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriesRepository extends JpaRepository<CategoryEntity, String> {
    Optional<CategoryEntity> findByCategoryName(String categoryName);
    List<CategoryEntity> findAllByDeleteFlag(Boolean deleteFlag);
    int countByCategoryName(String CategoryName);
}
