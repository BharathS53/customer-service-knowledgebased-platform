package com.example.customerservice.controller;

import com.example.customerservice.dto.ArticleRecommendationDto;
import com.example.customerservice.dto.ArticleRequestDto;
import com.example.customerservice.dto.ArticleResponseDto;
import com.example.customerservice.entity.ArticleStatus;
import com.example.customerservice.security.CustomUserDetails;
import com.example.customerservice.service.KnowledgeArticleService;
import com.example.customerservice.service.RecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class KnowledgeArticleRestController {

    private final KnowledgeArticleService articleService;
    private final RecommendationService recommendationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<ArticleResponseDto> createArticle(
            @Valid @RequestBody ArticleRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user) {
        ArticleResponseDto created = articleService.createArticle(request, user.getId());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ArticleResponseDto>> getAllArticles(
            @RequestParam(value = "category", required = false) Long categoryId,
            @RequestParam(value = "status", required = false) String status) {

        if (categoryId != null) {
            return ResponseEntity.ok(articleService.getArticlesByCategory(categoryId));
        }
        if ("ALL".equalsIgnoreCase(status)) {
            return ResponseEntity.ok(articleService.getAllArticles());
        }
        return ResponseEntity.ok(articleService.getPublishedArticles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticleResponseDto> getArticleById(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.getArticleById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ArticleResponseDto>> searchArticles(@RequestParam("keyword") String keyword) {
        return ResponseEntity.ok(articleService.searchArticles(keyword));
    }

    @GetMapping("/recommend")
    public ResponseEntity<List<ArticleRecommendationDto>> recommendArticles(
            @RequestParam("ticketId") Long ticketId,
            @RequestParam(value = "limit", defaultValue = "5") int limit) {
        return ResponseEntity.ok(recommendationService.getRecommendationsForTicket(ticketId, limit));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<ArticleResponseDto> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody ArticleRequestDto request) {
        return ResponseEntity.ok(articleService.updateArticle(id, request));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArticleResponseDto> updateArticleStatus(
            @PathVariable Long id,
            @RequestParam("status") ArticleStatus status) {
        return ResponseEntity.ok(articleService.updateArticleStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return ResponseEntity.noContent().build();
    }
}
