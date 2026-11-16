package com.example.customerservice.controller;

import com.example.customerservice.dto.DashboardStatsDto;
import com.example.customerservice.dto.RegisterRequest;
import com.example.customerservice.dto.TicketAssignRequest;
import com.example.customerservice.entity.ArticleCategory;
import com.example.customerservice.exception.BadRequestException;
import com.example.customerservice.security.CustomUserDetails;
import com.example.customerservice.service.ArticleCategoryService;
import com.example.customerservice.service.DashboardService;
import com.example.customerservice.service.TicketService;
import com.example.customerservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final DashboardService dashboardService;
    private final UserService userService;
    private final ArticleCategoryService categoryService;
    private final TicketService ticketService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        DashboardStatsDto stats = dashboardService.getDashboardStatistics();
        model.addAttribute("user", user);
        model.addAttribute("stats", stats);
        model.addAttribute("recentTickets", ticketService.getAllTickets());
        model.addAttribute("agents", userService.getAllAgents());
        return "admin/dashboard";
    }

    // -------------------------------------------------------------------------
    // User & Agent Management
    // -------------------------------------------------------------------------
    @GetMapping("/users")
    public String userManagement(
            @RequestParam(value = "role", required = false) String role,
            @AuthenticationPrincipal CustomUserDetails user,
            Model model) {

        model.addAttribute("user", user);
        model.addAttribute("currentRoleFilter", role != null ? role.toUpperCase() : "ALL");

        if ("AGENT".equalsIgnoreCase(role)) {
            model.addAttribute("usersList", userService.getAllAgents());
        } else if ("CUSTOMER".equalsIgnoreCase(role)) {
            model.addAttribute("usersList", userService.getAllCustomers());
        } else {
            model.addAttribute("usersList", userService.getAllUsers());
        }

        model.addAttribute("newUser", new RegisterRequest());
        return "admin/users";
    }

    @PostMapping("/users/create")
    public String createUser(
            @Valid @ModelAttribute("newUser") RegisterRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Validation failed: Please ensure all required fields are filled correctly.");
            return "redirect:/admin/users";
        }

        try {
            userService.registerUser(request);
            redirectAttributes.addFlashAttribute("successMessage", "User '" + request.getUsername() + "' created successfully!");
        } catch (BadRequestException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleUserStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "User status updated successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    // -------------------------------------------------------------------------
    // Category Management
    // -------------------------------------------------------------------------
    @GetMapping("/categories")
    public String categoryManagement(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("user", user);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/categories";
    }

    @PostMapping("/categories/create")
    public String createCategory(
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            RedirectAttributes redirectAttributes) {

        try {
            categoryService.createCategory(name, description);
            redirectAttributes.addFlashAttribute("successMessage", "Category '" + name + "' created successfully!");
        } catch (BadRequestException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/update")
    public String updateCategory(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            RedirectAttributes redirectAttributes) {

        try {
            categoryService.updateCategory(id, name, description);
            redirectAttributes.addFlashAttribute("successMessage", "Category updated successfully!");
        } catch (BadRequestException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete category: it is referenced by existing articles or tickets.");
        }
        return "redirect:/admin/categories";
    }

    // -------------------------------------------------------------------------
    // Ticket Management & Assignment
    // -------------------------------------------------------------------------
    @GetMapping("/tickets")
    public String tickets(
            @RequestParam(value = "status", required = false) String status,
            @AuthenticationPrincipal CustomUserDetails user,
            Model model) {

        model.addAttribute("user", user);
        model.addAttribute("currentStatusFilter", status != null ? status.toUpperCase() : "ALL");
        model.addAttribute("tickets", ticketService.getAllTickets());
        model.addAttribute("agents", userService.getAllAgents());
        return "admin/tickets";
    }

    @PostMapping("/tickets/{id}/assign")
    public String assignTicket(
            @PathVariable Long id,
            @RequestParam("agentId") Long agentId,
            @AuthenticationPrincipal CustomUserDetails user,
            RedirectAttributes redirectAttributes) {

        try {
            TicketAssignRequest request = new TicketAssignRequest(agentId);
            ticketService.assignTicket(id, request, user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Ticket successfully assigned to agent!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/tickets";
    }
}
