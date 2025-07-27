package com.hungpham.service.impl;

import com.hungpham.common.exception.BadRequestException;
import com.hungpham.dtos.CategoryDto;
import com.hungpham.dtos.UserDto;
import com.hungpham.entity.CategoryEntity;
import com.hungpham.mappers.CategoryMapper;
import com.hungpham.repository.CategoriesRepository;
import com.hungpham.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class CategoryServiceImpl implements CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoriesRepository categoriesRepository;

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<CategoryDto> getAllCategories(String status) {
        logger.info("Start get all category");
        List<CategoryEntity> categoryEntityList;
        List<CategoryDto> categoryDtoList = new ArrayList<>();
        if(status.equalsIgnoreCase("all")) {
            categoryEntityList =  categoriesRepository.findAll();
        } else {
            categoryEntityList = categoriesRepository.findAllByDeleteFlag(Boolean.valueOf(status));
        }
        for (CategoryEntity category : categoryEntityList) {
            categoryDtoList.add(categoryMapper.toDto(category));
        }
        return categoryDtoList;
    }

    @Override
    public CategoryDto getCategoryById(String id) {
        logger.info("Find category id {}", id);
        CategoryEntity categoryEntity =  categoriesRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Category not found " + id));
        return  categoryMapper.toDto(categoryEntity);
    }

    @Override
    public CategoryDto createCategory(CategoryDto categoryDto) {
        checkDuplicateCategory(categoryDto);
        UUID uuid = UUID.randomUUID();
        categoryDto.setId(String.valueOf(uuid));
        categoryDto.setCreatedDate(new Date().toString());
        logger.info("New category data {}", categoryDto);
        CategoryEntity categoryEntity = categoryMapper.toEntity(categoryDto);
        return  categoryMapper.toDto(categoriesRepository.save(categoryEntity));
    }

    @Override
    public CategoryDto updateCategory(CategoryDto categoryDto) {
        logger.info("Update category id {}", categoryDto.getId());
        if (null == categoryDto.getId()) {
            throw new BadRequestException("Not found category id to update");
        }
        CategoryEntity categoryEntity = categoriesRepository.findById(categoryDto.getId()).orElseThrow(
                ()-> new EntityNotFoundException("Category not found " + categoryDto.getId()));
        logger.info("Category data is {}", categoryDto);
        categoryDto.setCreatedDate(categoryEntity.getCreatedDate().toString());
        categoryDto.setUpdatedDate(new Date().toString());
        CategoryEntity finalNewEntity = categoryMapper.toEntity(categoryDto);
        return categoryMapper.toDto(categoriesRepository.save(finalNewEntity));
    }

    @Override
    public CategoryDto deleteCategory(String id) {
        logger.info("Delete category id {}", id);
        CategoryEntity categoryEntity = categoriesRepository.findById(id).orElseThrow(
                ()-> new EntityNotFoundException("News not found " + id));
        if (categoryEntity.isDeleteFlag()){
            return  categoryMapper.toDto(categoryEntity);
        }
        categoryEntity.setDeleteFlag(true);
        categoryEntity.setUpdatedDate(new Date().toString());
        return  categoryMapper.toDto(categoriesRepository.save(categoryEntity));
    }

    private void checkDuplicateCategory(CategoryDto categoryDto){
        int existFlag = categoriesRepository.countByCategoryName(categoryDto.getCategoryName());
        logger.info("Category check: {}. Flag is {}", categoryDto.getCategoryName(), existFlag);
        if (existFlag != 0){
            throw new BadRequestException("This category is already exist");
        }
    }
}
