package com.example.customerservice.controller;

import com.example.customerservice.dto.*;
import com.example.customerservice.security.CustomUserDetails;
import com.example.customerservice.service.TicketCommentService;
import com.example.customerservice.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketRestController {

    private final TicketService ticketService;
    private final TicketCommentService commentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<TicketResponseDto> createTicket(
            @Valid @RequestBody TicketRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user) {
        TicketResponseDto created = ticketService.createTicket(request, user.getId());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TicketResponseDto>> getTickets(
            @RequestParam(value = "status", required = false) String status,
            @AuthenticationPrincipal CustomUserDetails user) {

        boolean isStaff = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(r -> r.equals("ROLE_ADMIN") || r.equals("ROLE_AGENT"));

        if (!isStaff) {
            // Customer only sees their own tickets
            return ResponseEntity.ok(ticketService.getTicketsByCustomer(user.getId()));
        }

        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketDetailDto> getTicketById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {

        boolean isStaff = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(r -> r.equals("ROLE_ADMIN") || r.equals("ROLE_AGENT"));

        return ResponseEntity.ok(ticketService.getTicketDetails(id, isStaff));
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<TicketResponseDto> assignTicket(
            @PathVariable Long id,
            @Valid @RequestBody TicketAssignRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(ticketService.assignTicket(id, request, user.getId()));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<TicketResponseDto> updateTicketStatus(
            @PathVariable Long id,
            @Valid @RequestBody TicketStatusUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(ticketService.updateTicketStatus(id, request, user.getId()));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponseDto> addComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user) {

        boolean isStaff = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(r -> r.equals("ROLE_ADMIN") || r.equals("ROLE_AGENT"));

        if (!isStaff) {
            request.setIsInternal(false);
        }

        CommentResponseDto comment = commentService.addComment(id, request, user.getId());
        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentResponseDto>> getComments(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {

        boolean isStaff = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(r -> r.equals("ROLE_ADMIN") || r.equals("ROLE_AGENT"));

        return ResponseEntity.ok(commentService.getCommentsForTicket(id, isStaff));
    }

    @PostMapping("/{id}/link-article/{articleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<Void> linkArticle(
            @PathVariable Long id,
            @PathVariable Long articleId,
            @AuthenticationPrincipal CustomUserDetails user) {
        ticketService.linkArticleToTicket(id, articleId, user.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/link-article/{articleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<Void> unlinkArticle(
            @PathVariable Long id,
            @PathVariable Long articleId) {
        ticketService.unlinkArticleFromTicket(id, articleId);
        return ResponseEntity.noContent().build();
    }
}
