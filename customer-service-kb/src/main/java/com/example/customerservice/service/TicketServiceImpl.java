package com.example.customerservice.service;

import com.example.customerservice.dto.*;
import com.example.customerservice.entity.*;
import com.example.customerservice.exception.BadRequestException;
import com.example.customerservice.exception.ResourceNotFoundException;
import com.example.customerservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketServiceImpl implements TicketService {

    private final SupportTicketRepository ticketRepository;
    private final ArticleCategoryRepository categoryRepository;
    private final TicketHistoryRepository historyRepository;
    private final TicketArticleMappingRepository articleMappingRepository;
    private final KnowledgeArticleRepository articleRepository;
    private final UserService userService;

    @Override
    public TicketResponseDto createTicket(TicketRequestDto request, Long customerUserId) {
        User customer = userService.getUserEntityById(customerUserId);

        ArticleCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));

        TicketPriority priority = TicketPriority.MEDIUM;
        if (request.getPriority() != null && !request.getPriority().isBlank()) {
            priority = TicketPriority.valueOf(request.getPriority().toUpperCase());
        }

        String ticketNumber = generateTicketNumber();

        SupportTicket ticket = SupportTicket.builder()
                .ticketNumber(ticketNumber)
                .customer(customer)
                .category(category)
                .issueTitle(request.getIssueTitle().trim())
                .issueDescription(request.getIssueDescription().trim())
                .priority(priority)
                .status(TicketStatus.OPEN)
                .build();

        SupportTicket savedTicket = ticketRepository.save(ticket);

        // Record Initial History Audit Entry
        recordHistory(savedTicket, customer, "STATUS", null, TicketStatus.OPEN.name(), "Ticket submitted by customer");

        return mapToDto(savedTicket);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponseDto getTicketById(Long ticketId) {
        return mapToDto(getTicketEntityById(ticketId));
    }

    @Override
    @Transactional(readOnly = true)
    public TicketDetailDto getTicketDetails(Long ticketId, boolean includeInternalComments) {
        SupportTicket ticket = getTicketEntityById(ticketId);

        List<CommentResponseDto> commentDtos = new ArrayList<>();
        if (ticket.getComments() != null) {
            for (TicketComment c : ticket.getComments()) {
                if (includeInternalComments || !Boolean.TRUE.equals(c.getIsInternal())) {
                    String role = c.getUser().getRoles().stream()
                            .findFirst()
                            .map(r -> r.getName().name())
                            .orElse("USER");

                    commentDtos.add(CommentResponseDto.builder()
                            .id(c.getId())
                            .ticketId(ticket.getId())
                            .userId(c.getUser().getId())
                            .userName(c.getUser().getFullName())
                            .userRole(role)
                            .commentText(c.getCommentText())
                            .isInternal(c.getIsInternal())
                            .createdAt(c.getCreatedAt())
                            .build());
                }
            }
        }

        List<ArticleResponseDto> articleDtos = new ArrayList<>();
        if (ticket.getLinkedArticleMappings() != null) {
            for (TicketArticleMapping m : ticket.getLinkedArticleMappings()) {
                KnowledgeArticle a = m.getArticle();
                articleDtos.add(ArticleResponseDto.builder()
                        .id(a.getId())
                        .title(a.getTitle())
                        .description(a.getDescription())
                        .categoryId(a.getCategory().getId())
                        .categoryName(a.getCategory().getName())
                        .problemStatement(a.getProblemStatement())
                        .rootCause(a.getRootCause())
                        .stepByStepSolution(a.getStepByStepSolution())
                        .status(a.getStatus().name())
                        .createdById(a.getCreatedBy().getId())
                        .createdByName(a.getCreatedBy().getFullName())
                        .tags(a.getTags().stream().map(ArticleTag::getName).collect(Collectors.toSet()))
                        .createdAt(a.getCreatedAt())
                        .updatedAt(a.getUpdatedAt())
                        .build());
            }
        }

        List<TicketHistoryDto> historyDtos = new ArrayList<>();
        if (ticket.getHistoryRecords() != null) {
            for (TicketHistory h : ticket.getHistoryRecords()) {
                historyDtos.add(TicketHistoryDto.builder()
                        .id(h.getId())
                        .ticketId(ticket.getId())
                        .changedById(h.getChangedBy().getId())
                        .changedByName(h.getChangedBy().getFullName())
                        .fieldChanged(h.getFieldChanged())
                        .oldValue(h.getOldValue())
                        .newValue(h.getNewValue())
                        .changeDescription(h.getChangeDescription())
                        .createdAt(h.getCreatedAt())
                        .build());
            }
        }

        return TicketDetailDto.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .customerId(ticket.getCustomer().getId())
                .customerName(ticket.getCustomer().getFullName())
                .customerEmail(ticket.getCustomer().getEmail())
                .assignedAgentId(ticket.getAssignedAgent() != null ? ticket.getAssignedAgent().getId() : null)
                .assignedAgentName(ticket.getAssignedAgent() != null ? ticket.getAssignedAgent().getFullName() : "Unassigned")
                .categoryId(ticket.getCategory().getId())
                .categoryName(ticket.getCategory().getName())
                .issueTitle(ticket.getIssueTitle())
                .issueDescription(ticket.getIssueDescription())
                .priority(ticket.getPriority().name())
                .status(ticket.getStatus().name())
                .resolutionDetails(ticket.getResolutionDetails())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .comments(commentDtos)
                .linkedArticles(articleDtos)
                .history(historyDtos)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponseDto getTicketByNumber(String ticketNumber) {
        SupportTicket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with number: " + ticketNumber));
        return mapToDto(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public SupportTicket getTicketEntityById(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponseDto> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponseDto> getTicketsByCustomer(Long customerId) {
        return ticketRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponseDto> getTicketsByAgent(Long agentId) {
        return ticketRepository.findByAssignedAgentIdOrderByCreatedAtDesc(agentId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponseDto> getTicketsByStatus(TicketStatus status) {
        return ticketRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public TicketResponseDto assignTicket(Long ticketId, TicketAssignRequest request, Long changedByUserId) {
        SupportTicket ticket = getTicketEntityById(ticketId);
        User agent = userService.getUserEntityById(request.getAgentId());
        User changedBy = userService.getUserEntityById(changedByUserId);

        String oldAgentName = ticket.getAssignedAgent() != null ? ticket.getAssignedAgent().getFullName() : "Unassigned";
        ticket.setAssignedAgent(agent);
        if (ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }

        SupportTicket updated = ticketRepository.save(ticket);

        recordHistory(updated, changedBy, "ASSIGNED_AGENT", oldAgentName, agent.getFullName(),
                "Assigned to support agent " + agent.getFullName());

        return mapToDto(updated);
    }

    @Override
    public TicketResponseDto updateTicketStatus(Long ticketId, TicketStatusUpdateRequest request, Long changedByUserId) {
        SupportTicket ticket = getTicketEntityById(ticketId);
        User changedBy = userService.getUserEntityById(changedByUserId);

        TicketStatus oldStatus = ticket.getStatus();
        TicketStatus newStatus = TicketStatus.valueOf(request.getStatus().toUpperCase());

        ticket.setStatus(newStatus);
        if (request.getResolutionDetails() != null && !request.getResolutionDetails().isBlank()) {
            ticket.setResolutionDetails(request.getResolutionDetails().trim());
        }

        SupportTicket updated = ticketRepository.save(ticket);

        String description = request.getNote() != null ? request.getNote() : "Status updated from " + oldStatus + " to " + newStatus;
        recordHistory(updated, changedBy, "STATUS", oldStatus.name(), newStatus.name(), description);

        return mapToDto(updated);
    }

    @Override
    public void linkArticleToTicket(Long ticketId, Long articleId, Long linkedByUserId) {
        SupportTicket ticket = getTicketEntityById(ticketId);
        KnowledgeArticle article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Knowledge Article not found with ID: " + articleId));
        User linkedBy = userService.getUserEntityById(linkedByUserId);

        TicketArticleMappingId id = new TicketArticleMappingId(ticketId, articleId);
        if (articleMappingRepository.existsById(id)) {
            throw new BadRequestException("This article is already linked to the ticket");
        }

        TicketArticleMapping mapping = TicketArticleMapping.builder()
                .id(id)
                .ticket(ticket)
                .article(article)
                .linkedBy(linkedBy)
                .build();

        articleMappingRepository.save(mapping);

        recordHistory(ticket, linkedBy, "LINKED_ARTICLE", null, article.getTitle(),
                "Linked resolution guide: " + article.getTitle());
    }

    @Override
    public void unlinkArticleFromTicket(Long ticketId, Long articleId) {
        TicketArticleMappingId id = new TicketArticleMappingId(ticketId, articleId);
        if (!articleMappingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Link between ticket and article not found");
        }
        articleMappingRepository.deleteById(id);
    }

    private void recordHistory(SupportTicket ticket, User changedBy, String field, String oldVal, String newVal, String description) {
        TicketHistory history = TicketHistory.builder()
                .ticket(ticket)
                .changedBy(changedBy)
                .fieldChanged(field)
                .oldValue(oldVal)
                .newValue(newVal)
                .changeDescription(description)
                .build();
        historyRepository.save(history);
    }

    private String generateTicketNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomSuffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "TCK-" + datePart + "-" + randomSuffix;
    }

    private TicketResponseDto mapToDto(SupportTicket ticket) {
        return TicketResponseDto.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .customerId(ticket.getCustomer().getId())
                .customerName(ticket.getCustomer().getFullName())
                .customerEmail(ticket.getCustomer().getEmail())
                .assignedAgentId(ticket.getAssignedAgent() != null ? ticket.getAssignedAgent().getId() : null)
                .assignedAgentName(ticket.getAssignedAgent() != null ? ticket.getAssignedAgent().getFullName() : "Unassigned")
                .categoryId(ticket.getCategory().getId())
                .categoryName(ticket.getCategory().getName())
                .issueTitle(ticket.getIssueTitle())
                .issueDescription(ticket.getIssueDescription())
                .priority(ticket.getPriority().name())
                .status(ticket.getStatus().name())
                .resolutionDetails(ticket.getResolutionDetails())
                .commentsCount(ticket.getComments() != null ? ticket.getComments().size() : 0)
                .linkedArticlesCount(ticket.getLinkedArticleMappings() != null ? ticket.getLinkedArticleMappings().size() : 0)
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }
}
