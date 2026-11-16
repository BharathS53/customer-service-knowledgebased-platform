package com.example.customerservice.service;

import com.example.customerservice.entity.ArticleCategory;

import java.util.List;

public interface ArticleCategoryService {
    ArticleCategory createCategory(String name, String description);
    ArticleCategory getCategoryById(Long id);
    List<ArticleCategory> getAllCategories();
    ArticleCategory updateCategory(Long id, String name, String description);
    void deleteCategory(Long id);
}
