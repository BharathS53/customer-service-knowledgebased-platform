package com.example.customerservice.controller;

import com.example.customerservice.dto.ArticleResponseDto;
import com.example.customerservice.dto.TicketResponseDto;
import com.example.customerservice.entity.TicketStatus;
import com.example.customerservice.security.CustomUserDetails;
import com.example.customerservice.service.ArticleCategoryService;
import com.example.customerservice.service.KnowledgeArticleService;
import com.example.customerservice.service.RecommendationService;
import com.example.customerservice.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/agent")
@PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
@RequiredArgsConstructor
public class AgentPortalController {

    private final TicketService ticketService;
    private final KnowledgeArticleService articleService;
    private final ArticleCategoryService categoryService;
    private final RecommendationService recommendationService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        List<TicketResponseDto> myTickets = ticketService.getTicketsByAgent(user.getId());

        long openCount = myTickets.stream().filter(t -> "OPEN".equals(t.getStatus())).count();
        long inProgressCount = myTickets.stream().filter(t -> "IN_PROGRESS".equals(t.getStatus())).count();
        long resolvedCount = myTickets.stream().filter(t -> "RESOLVED".equals(t.getStatus())).count();

        model.addAttribute("user", user);
        model.addAttribute("totalAssigned", myTickets.size());
        model.addAttribute("openCount", openCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("resolvedCount", resolvedCount);
        model.addAttribute("recentTickets", myTickets.stream().limit(5).toList());
        model.addAttribute("recommendedArticles", articleService.getPublishedArticles().stream().limit(4).toList());
        return "agent/dashboard";
    }

    @GetMapping("/tickets")
    public String assignedTickets(
            @RequestParam(value = "status", required = false) String status,
            @AuthenticationPrincipal CustomUserDetails user,
            Model model) {

        List<TicketResponseDto> tickets = ticketService.getTicketsByAgent(user.getId());

        if (status != null && !status.equalsIgnoreCase("ALL")) {
            tickets = tickets.stream()
                    .filter(t -> t.getStatus().equalsIgnoreCase(status))
                    .toList();
        }

        model.addAttribute("user", user);
        model.addAttribute("tickets", tickets);
        model.addAttribute("selectedStatus", status != null ? status.toUpperCase() : "ALL");
        return "agent/tickets";
    }

    @GetMapping("/knowledge")
    public String knowledgeRepository(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) Long categoryId,
            @RequestParam(value = "tag", required = false) String tag,
            @AuthenticationPrincipal CustomUserDetails user,
            Model model) {

        List<ArticleResponseDto> articles;
        if (tag != null && !tag.trim().isBlank()) {
            articles = articleService.getArticlesByTag(tag.trim());
        } else if (keyword != null && !keyword.trim().isBlank()) {
            articles = articleService.searchArticles(keyword.trim());
        } else if (categoryId != null) {
            articles = articleService.getArticlesByCategory(categoryId);
        } else {
            articles = articleService.getPublishedArticles();
        }

        model.addAttribute("user", user);
        model.addAttribute("articles", articles);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("tags", recommendationService.getAllTags());
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("selectedTag", tag != null ? tag.trim() : null);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        return "agent/knowledge";
    }
}
