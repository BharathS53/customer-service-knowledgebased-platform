package com.example.customerservice.service;

import com.example.customerservice.entity.ArticleCategory;
import com.example.customerservice.exception.BadRequestException;
import com.example.customerservice.exception.ResourceNotFoundException;
import com.example.customerservice.repository.ArticleCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleCategoryServiceImpl implements ArticleCategoryService {

    private final ArticleCategoryRepository categoryRepository;

    @Override
    public ArticleCategory createCategory(String name, String description) {
        if (categoryRepository.existsByNameIgnoreCase(name.trim())) {
            throw new BadRequestException("Category '" + name + "' already exists");
        }
        ArticleCategory category = ArticleCategory.builder()
                .name(name.trim())
                .description(description != null ? description.trim() : null)
                .build();
        return categoryRepository.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleCategory getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public ArticleCategory updateCategory(Long id, String name, String description) {
        ArticleCategory category = getCategoryById(id);
        if (!category.getName().equalsIgnoreCase(name.trim()) &&
                categoryRepository.existsByNameIgnoreCase(name.trim())) {
            throw new BadRequestException("Category '" + name + "' already exists");
        }
        category.setName(name.trim());
        category.setDescription(description != null ? description.trim() : null);
        return categoryRepository.save(category);
    }

    @Override
    public void deleteCategory(Long id) {
        ArticleCategory category = getCategoryById(id);
        categoryRepository.delete(category);
    }
}
