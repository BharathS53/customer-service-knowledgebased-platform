package com.example.customerservice.service;

import com.example.customerservice.dto.ArticleRequestDto;
import com.example.customerservice.dto.ArticleResponseDto;
import com.example.customerservice.entity.*;
import com.example.customerservice.exception.ResourceNotFoundException;
import com.example.customerservice.repository.ArticleCategoryRepository;
import com.example.customerservice.repository.ArticleTagRepository;
import com.example.customerservice.repository.KnowledgeArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class KnowledgeArticleServiceImpl implements KnowledgeArticleService {

    private final KnowledgeArticleRepository articleRepository;
    private final ArticleCategoryRepository categoryRepository;
    private final ArticleTagRepository tagRepository;
    private final UserService userService;

    @Override
    public ArticleResponseDto createArticle(ArticleRequestDto request, Long creatorUserId) {
        ArticleCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));

        User creator = userService.getUserEntityById(creatorUserId);

        ArticleStatus status = ArticleStatus.DRAFT;
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            status = ArticleStatus.valueOf(request.getStatus().toUpperCase());
        }

        Set<ArticleTag> tags = resolveTags(request.getTags());

        KnowledgeArticle article = KnowledgeArticle.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .category(category)
                .problemStatement(request.getProblemStatement().trim())
                .rootCause(request.getRootCause().trim())
                .stepByStepSolution(request.getStepByStepSolution().trim())
                .status(status)
                .createdBy(creator)
                .tags(tags)
                .build();

        KnowledgeArticle saved = articleRepository.save(article);
        return mapToDto(saved);
    }

    @Override
    public ArticleResponseDto updateArticle(Long articleId, ArticleRequestDto request) {
        KnowledgeArticle article = getArticleEntityById(articleId);

        ArticleCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));

        article.setTitle(request.getTitle().trim());
        article.setDescription(request.getDescription().trim());
        article.setCategory(category);
        article.setProblemStatement(request.getProblemStatement().trim());
        article.setRootCause(request.getRootCause().trim());
        article.setStepByStepSolution(request.getStepByStepSolution().trim());

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            article.setStatus(ArticleStatus.valueOf(request.getStatus().toUpperCase()));
        }

        if (request.getTags() != null) {
            article.setTags(resolveTags(request.getTags()));
        }

        KnowledgeArticle updated = articleRepository.save(article);
        return mapToDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleResponseDto getArticleById(Long articleId) {
        return mapToDto(getArticleEntityById(articleId));
    }

    @Override
    @Transactional(readOnly = true)
    public KnowledgeArticle getArticleEntityById(Long articleId) {
        return articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Knowledge Article not found with ID: " + articleId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleResponseDto> getAllArticles() {
        return articleRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleResponseDto> getPublishedArticles() {
        return articleRepository.findByStatus(ArticleStatus.PUBLISHED).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleResponseDto> getArticlesByCategory(Long categoryId) {
        return articleRepository.findByCategoryIdAndStatus(categoryId, ArticleStatus.PUBLISHED).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleResponseDto> getArticlesByTag(String tagName) {
        if (tagName == null || tagName.trim().isBlank()) {
            return getPublishedArticles();
        }
        return articleRepository.findPublishedByTagName(tagName.trim()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleResponseDto> searchArticles(String keyword) {
        if (keyword == null || keyword.trim().isBlank()) {
            return getPublishedArticles();
        }
        return articleRepository.searchPublishedArticles(keyword.trim()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteArticle(Long articleId) {
        KnowledgeArticle article = getArticleEntityById(articleId);
        articleRepository.delete(article);
    }

    @Override
    public ArticleResponseDto updateArticleStatus(Long articleId, ArticleStatus status) {
        KnowledgeArticle article = getArticleEntityById(articleId);
        article.setStatus(status);
        KnowledgeArticle updated = articleRepository.save(article);
        return mapToDto(updated);
    }

    private Set<ArticleTag> resolveTags(Set<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new HashSet<>();
        }
        Set<ArticleTag> resolvedTags = new HashSet<>();
        for (String tagName : tagNames) {
            String cleanName = tagName.trim().toLowerCase();
            if (!cleanName.isBlank()) {
                ArticleTag tag = tagRepository.findByNameIgnoreCase(cleanName)
                        .orElseGet(() -> tagRepository.save(ArticleTag.builder().name(cleanName).build()));
                resolvedTags.add(tag);
            }
        }
        return resolvedTags;
    }

    private ArticleResponseDto mapToDto(KnowledgeArticle article) {
        Set<String> tagNames = article.getTags().stream()
                .map(ArticleTag::getName)
                .collect(Collectors.toSet());

        return ArticleResponseDto.builder()
                .id(article.getId())
                .title(article.getTitle())
                .description(article.getDescription())
                .categoryId(article.getCategory().getId())
                .categoryName(article.getCategory().getName())
                .problemStatement(article.getProblemStatement())
                .rootCause(article.getRootCause())
                .stepByStepSolution(article.getStepByStepSolution())
                .status(article.getStatus().name())
                .createdById(article.getCreatedBy().getId())
                .createdByName(article.getCreatedBy().getFullName())
                .tags(tagNames)
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .build();
    }
}
