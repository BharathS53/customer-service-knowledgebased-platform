package com.example.customerservice.controller;

import com.example.customerservice.dto.ArticleRequestDto;
import com.example.customerservice.dto.ArticleResponseDto;
import com.example.customerservice.entity.ArticleStatus;
import com.example.customerservice.exception.BadRequestException;
import com.example.customerservice.security.CustomUserDetails;
import com.example.customerservice.service.ArticleCategoryService;
import com.example.customerservice.service.KnowledgeArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ArticleWebController {

    private final KnowledgeArticleService articleService;
    private final ArticleCategoryService categoryService;

    // -------------------------------------------------------------------------
    // Admin Articles Management
    // -------------------------------------------------------------------------
    @GetMapping("/admin/articles")
    @PreAuthorize("hasRole('ADMIN')")
    public String listArticles(
            @RequestParam(value = "category", required = false) Long categoryId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "keyword", required = false) String keyword,
            @AuthenticationPrincipal CustomUserDetails user,
            Model model) {

        List<ArticleResponseDto> articles;
        if (keyword != null && !keyword.trim().isBlank()) {
            articles = articleService.searchArticles(keyword.trim());
        } else if (categoryId != null) {
            articles = articleService.getArticlesByCategory(categoryId);
        } else {
            articles = articleService.getAllArticles();
        }

        if (status != null && !status.equalsIgnoreCase("ALL")) {
            articles = articles.stream()
                    .filter(a -> a.getStatus().equalsIgnoreCase(status))
                    .toList();
        }

        model.addAttribute("user", user);
        model.addAttribute("articles", articles);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("selectedStatus", status != null ? status.toUpperCase() : "ALL");
        model.addAttribute("keyword", keyword != null ? keyword : "");
        return "admin/articles/list";
    }

    @GetMapping("/admin/articles/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateForm(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("user", user);
        model.addAttribute("articleForm", new ArticleRequestDto());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("isEdit", false);
        return "admin/articles/form";
    }

    @PostMapping("/admin/articles/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String processCreate(
            @Valid @ModelAttribute("articleForm") ArticleRequestDto request,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails user,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("isEdit", false);
            return "admin/articles/form";
        }

        try {
            ArticleResponseDto created = articleService.createArticle(request, user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Article '" + created.getTitle() + "' created successfully!");
            return "redirect:/admin/articles";
        } catch (BadRequestException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("user", user);
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("isEdit", false);
            return "admin/articles/form";
        }
    }

    @GetMapping("/admin/articles/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String viewArticleAdmin(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user, Model model) {
        ArticleResponseDto article = articleService.getArticleById(id);
        model.addAttribute("user", user);
        model.addAttribute("article", article);
        return "admin/articles/view";
    }

    @GetMapping("/admin/articles/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditForm(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user, Model model) {
        ArticleResponseDto article = articleService.getArticleById(id);

        ArticleRequestDto form = ArticleRequestDto.builder()
                .title(article.getTitle())
                .description(article.getDescription())
                .categoryId(article.getCategoryId())
                .problemStatement(article.getProblemStatement())
                .rootCause(article.getRootCause())
                .stepByStepSolution(article.getStepByStepSolution())
                .status(article.getStatus())
                .tags(article.getTags())
                .tagsInput(article.getTags() != null ? String.join(", ", article.getTags()) : "")
                .build();

        model.addAttribute("user", user);
        model.addAttribute("articleId", id);
        model.addAttribute("articleForm", form);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("isEdit", true);
        return "admin/articles/form";
    }

    @PostMapping("/admin/articles/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String processEdit(
            @PathVariable Long id,
            @Valid @ModelAttribute("articleForm") ArticleRequestDto request,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails user,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("articleId", id);
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("isEdit", true);
            return "admin/articles/form";
        }

        try {
            articleService.updateArticle(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Article updated successfully!");
            return "redirect:/admin/articles/" + id;
        } catch (BadRequestException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("user", user);
            model.addAttribute("articleId", id);
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("isEdit", true);
            return "admin/articles/form";
        }
    }

    @PostMapping("/admin/articles/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteArticle(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            articleService.deleteArticle(id);
            redirectAttributes.addFlashAttribute("successMessage", "Article deleted successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting article: " + ex.getMessage());
        }
        return "redirect:/admin/articles";
    }

    @PostMapping("/admin/articles/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        ArticleResponseDto article = articleService.getArticleById(id);
        ArticleStatus newStatus = "PUBLISHED".equalsIgnoreCase(article.getStatus()) ? ArticleStatus.DRAFT : ArticleStatus.PUBLISHED;
        articleService.updateArticleStatus(id, newStatus);
        redirectAttributes.addFlashAttribute("successMessage", "Article status changed to " + newStatus);
        return "redirect:/admin/articles";
    }

    // -------------------------------------------------------------------------
    // General Public / Agent Article Viewer
    // -------------------------------------------------------------------------
    @GetMapping("/articles/{id}")
    public String publicViewArticle(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user, Model model) {
        ArticleResponseDto article = articleService.getArticleById(id);
        model.addAttribute("user", user);
        model.addAttribute("article", article);
        return "admin/articles/view";
    }
}
