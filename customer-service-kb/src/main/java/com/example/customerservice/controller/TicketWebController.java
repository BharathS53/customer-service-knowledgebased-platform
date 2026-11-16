package com.example.customerservice.controller;

import com.example.customerservice.dto.ArticleRecommendationDto;
import com.example.customerservice.dto.CommentRequestDto;
import com.example.customerservice.dto.TicketDetailDto;
import com.example.customerservice.dto.TicketStatusUpdateRequest;
import com.example.customerservice.exception.BadRequestException;
import com.example.customerservice.security.CustomUserDetails;
import com.example.customerservice.service.KnowledgeArticleService;
import com.example.customerservice.service.RecommendationService;
import com.example.customerservice.service.TicketCommentService;
import com.example.customerservice.service.TicketService;
import com.example.customerservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketWebController {

    private final TicketService ticketService;
    private final TicketCommentService commentService;
    private final KnowledgeArticleService articleService;
    private final UserService userService;
    private final RecommendationService recommendationService;

    @GetMapping("/{id}")
    public String viewTicketDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user,
            Model model) {

        boolean isStaff = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(r -> r.equals("ROLE_ADMIN") || r.equals("ROLE_AGENT"));

        TicketDetailDto ticket = ticketService.getTicketDetails(id, isStaff);
        List<ArticleRecommendationDto> recommendations = recommendationService.getRecommendationsForTicket(id, 4);

        model.addAttribute("user", user);
        model.addAttribute("isStaff", isStaff);
        model.addAttribute("ticket", ticket);
        model.addAttribute("recommendations", recommendations);
        model.addAttribute("availableArticles", articleService.getPublishedArticles());
        model.addAttribute("agents", userService.getAllAgents());
        return "tickets/detail";
    }

    @PostMapping("/{id}/comments")
    public String addComment(
            @PathVariable Long id,
            @RequestParam("commentText") String commentText,
            @RequestParam(value = "isInternal", required = false) Boolean isInternal,
            @AuthenticationPrincipal CustomUserDetails user,
            RedirectAttributes redirectAttributes) {

        boolean isStaff = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(r -> r.equals("ROLE_ADMIN") || r.equals("ROLE_AGENT"));

        if (!isStaff) {
            isInternal = false;
        }

        try {
            CommentRequestDto request = CommentRequestDto.builder()
                    .commentText(commentText)
                    .isInternal(isInternal != null && isInternal)
                    .build();
            commentService.addComment(id, request, user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Comment posted successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam("status") String status,
            @RequestParam(value = "resolutionDetails", required = false) String resolutionDetails,
            @RequestParam(value = "note", required = false) String note,
            @AuthenticationPrincipal CustomUserDetails user,
            RedirectAttributes redirectAttributes) {

        try {
            TicketStatusUpdateRequest request = new TicketStatusUpdateRequest(status, resolutionDetails, note);
            ticketService.updateTicketStatus(id, request, user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Ticket status transitioned to " + status + "!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/link-article")
    public String linkArticle(
            @PathVariable Long id,
            @RequestParam("articleId") Long articleId,
            @AuthenticationPrincipal CustomUserDetails user,
            RedirectAttributes redirectAttributes) {

        try {
            ticketService.linkArticleToTicket(id, articleId, user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Knowledge article linked to ticket resolution!");
        } catch (BadRequestException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/unlink-article")
    public String unlinkArticle(
            @PathVariable Long id,
            @RequestParam("articleId") Long articleId,
            RedirectAttributes redirectAttributes) {

        try {
            ticketService.unlinkArticleFromTicket(id, articleId);
            redirectAttributes.addFlashAttribute("successMessage", "Article unlinked from ticket.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/tickets/" + id;
    }
}
