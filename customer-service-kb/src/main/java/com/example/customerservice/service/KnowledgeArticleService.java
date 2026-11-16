package com.example.customerservice.service;

import com.example.customerservice.dto.ArticleRequestDto;
import com.example.customerservice.dto.ArticleResponseDto;
import com.example.customerservice.entity.ArticleStatus;
import com.example.customerservice.entity.KnowledgeArticle;

import java.util.List;

public interface KnowledgeArticleService {
    ArticleResponseDto createArticle(ArticleRequestDto request, Long creatorUserId);
    ArticleResponseDto updateArticle(Long articleId, ArticleRequestDto request);
    ArticleResponseDto getArticleById(Long articleId);
    KnowledgeArticle getArticleEntityById(Long articleId);
    List<ArticleResponseDto> getAllArticles();
    List<ArticleResponseDto> getPublishedArticles();
    List<ArticleResponseDto> getArticlesByCategory(Long categoryId);
    List<ArticleResponseDto> getArticlesByTag(String tagName);
    List<ArticleResponseDto> searchArticles(String keyword);
    void deleteArticle(Long articleId);
    ArticleResponseDto updateArticleStatus(Long articleId, ArticleStatus status);
}
