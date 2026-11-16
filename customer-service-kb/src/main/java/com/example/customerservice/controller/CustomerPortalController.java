package com.example.customerservice.controller;

import com.example.customerservice.dto.ArticleResponseDto;
import com.example.customerservice.dto.TicketRequestDto;
import com.example.customerservice.dto.TicketResponseDto;
import com.example.customerservice.exception.BadRequestException;
import com.example.customerservice.security.CustomUserDetails;
import com.example.customerservice.service.ArticleCategoryService;
import com.example.customerservice.service.KnowledgeArticleService;
import com.example.customerservice.service.RecommendationService;
import com.example.customerservice.service.TicketService;
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
@RequestMapping("/customer")
@PreAuthorize("hasRole('CUSTOMER')")
@RequiredArgsConstructor
public class CustomerPortalController {

    private final TicketService ticketService;
    private final KnowledgeArticleService articleService;
    private final ArticleCategoryService categoryService;
    private final RecommendationService recommendationService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        List<TicketResponseDto> myTickets = ticketService.getTicketsByCustomer(user.getId());
        long activeTickets = myTickets.stream().filter(t -> !"RESOLVED".equals(t.getStatus()) && !"CLOSED".equals(t.getStatus())).count();
        long resolvedTickets = myTickets.stream().filter(t -> "RESOLVED".equals(t.getStatus()) || "CLOSED".equals(t.getStatus())).count();

        model.addAttribute("user", user);
        model.addAttribute("totalTickets", myTickets.size());
        model.addAttribute("activeTickets", activeTickets);
        model.addAttribute("resolvedTickets", resolvedTickets);
        model.addAttribute("recentTickets", myTickets.stream().limit(5).toList());
        model.addAttribute("faqs", articleService.getPublishedArticles().stream().limit(4).toList());
        return "customer/dashboard";
    }

    @GetMapping("/tickets")
    public String myTickets(
            @RequestParam(value = "status", required = false) String status,
            @AuthenticationPrincipal CustomUserDetails user,
            Model model) {

        List<TicketResponseDto> tickets = ticketService.getTicketsByCustomer(user.getId());

        if (status != null && !status.equalsIgnoreCase("ALL")) {
            tickets = tickets.stream()
                    .filter(t -> t.getStatus().equalsIgnoreCase(status))
                    .toList();
        }

        model.addAttribute("user", user);
        model.addAttribute("tickets", tickets);
        model.addAttribute("selectedStatus", status != null ? status.toUpperCase() : "ALL");
        return "customer/tickets";
    }

    @GetMapping("/tickets/create")
    public String showCreateTicketForm(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("user", user);
        model.addAttribute("ticketForm", new TicketRequestDto());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "customer/create-ticket";
    }

    @PostMapping("/tickets/create")
    public String processCreateTicket(
            @Valid @ModelAttribute("ticketForm") TicketRequestDto request,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails user,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("categories", categoryService.getAllCategories());
            return "customer/create-ticket";
        }

        try {
            TicketResponseDto created = ticketService.createTicket(request, user.getId());
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Ticket " + created.getTicketNumber() + " created successfully! Our support agents have been notified.");
            return "redirect:/tickets/" + created.getId();
        } catch (BadRequestException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("user", user);
            model.addAttribute("categories", categoryService.getAllCategories());
            return "customer/create-ticket";
        }
    }

    @GetMapping("/knowledge")
    public String customerKnowledge(
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
        return "customer/knowledge";
    }
}
