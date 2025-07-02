package com.hungpham.service;

import com.hungpham.dtos.CategoryDto;
import com.hungpham.entity.CategoryEntity;

import java.util.List;

public interface CategoryService {
    List<CategoryEntity> getAllCategories(String status);
    CategoryEntity getCategoryById(String id);
    CategoryEntity createCategory(CategoryDto categoryDto);
    CategoryEntity updateCategory(CategoryDto categoryDto);
    CategoryEntity deleteCategory(String id);
}
