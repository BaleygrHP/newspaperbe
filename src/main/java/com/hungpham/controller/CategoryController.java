package com.hungpham.controller;

import com.hungpham.dtos.CategoryDto;
import com.hungpham.entity.CategoryEntity;
import com.hungpham.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("category")
// remove gấp sau khi config auth2.0
@CrossOrigin(origins = "http://localhost:3000") // Allow frontend origin
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/get-all-categories")
    public List<CategoryEntity> getAllCategories(@RequestParam String status) {
        return categoryService.getAllCategories(status);
    }

    @GetMapping("/get-by-id")
    public CategoryEntity getCategoryById(@RequestParam String id) {
        return categoryService.getCategoryById(id);
    }


    @PostMapping("/create-category")
    public CategoryEntity createNewNews(@RequestBody CategoryDto categoryDto){
        return categoryService.createCategory(categoryDto);
    }

    @PutMapping("/update-category")
    public CategoryEntity updateNews(@RequestBody CategoryDto categoryDto){
        return categoryService.updateCategory(categoryDto);
    }

    @DeleteMapping
    public CategoryEntity deleteCategory(@RequestParam String id){
        return categoryService.deleteCategory(id);
    }
}
