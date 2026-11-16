package com.example.customerservice.repository;

import com.example.customerservice.entity.ArticleCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleCategoryRepository extends JpaRepository<ArticleCategory, Long> {
    Optional<ArticleCategory> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
