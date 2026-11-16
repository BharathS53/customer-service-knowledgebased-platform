package com.example.customerservice.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for top-level entry routing.
 * Automatically forwards authenticated users to their role-specific dashboard,
 * or directs unauthenticated visitors to the login screen.
 */
@Controller
public class WebController {

    @GetMapping("/")
    public String index(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getPrincipal().equals("anonymousUser")) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                String role = authority.getAuthority();
                if ("ROLE_ADMIN".equals(role)) {
                    return "redirect:/admin/dashboard";
                } else if ("ROLE_AGENT".equals(role)) {
                    return "redirect:/agent/dashboard";
                } else if ("ROLE_CUSTOMER".equals(role)) {
                    return "redirect:/customer/dashboard";
                }
            }
        }
        return "redirect:/login";
    }
}
