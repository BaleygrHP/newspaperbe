package com.hungpham.service;

import com.hungpham.dtos.CategoryDto;
import com.hungpham.entity.CategoryEntity;

import java.util.List;

public interface CategoryService {
    List<CategoryDto> getAllCategories(String status);
    CategoryDto getCategoryById(String id);
    CategoryDto createCategory(CategoryDto categoryDto);
    CategoryDto updateCategory(CategoryDto categoryDto);
    CategoryDto deleteCategory(String id);
}
