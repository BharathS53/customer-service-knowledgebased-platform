package com.example.customerservice.service;

import com.example.customerservice.dto.DashboardStatsDto;
import com.example.customerservice.entity.ArticleStatus;
import com.example.customerservice.entity.RoleType;
import com.example.customerservice.entity.TicketStatus;
import com.example.customerservice.repository.KnowledgeArticleRepository;
import com.example.customerservice.repository.SupportTicketRepository;
import com.example.customerservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final SupportTicketRepository ticketRepository;
    private final KnowledgeArticleRepository articleRepository;
    private final UserRepository userRepository;

    @Override
    public DashboardStatsDto getDashboardStatistics() {
        long totalTickets = ticketRepository.count();
        long openTickets = ticketRepository.countByStatus(TicketStatus.OPEN);
        long inProgressTickets = ticketRepository.countByStatus(TicketStatus.IN_PROGRESS);
        long resolvedTickets = ticketRepository.countByStatus(TicketStatus.RESOLVED);
        long closedTickets = ticketRepository.countByStatus(TicketStatus.CLOSED);

        long totalArticles = articleRepository.count();
        long publishedArticles = articleRepository.countByStatus(ArticleStatus.PUBLISHED);

        long totalAgents = userRepository.countByRoles_Name(RoleType.ROLE_AGENT);
        long totalCustomers = userRepository.countByRoles_Name(RoleType.ROLE_CUSTOMER);

        Map<String, Long> ticketsByStatus = new HashMap<>();
        List<Object[]> statusCounts = ticketRepository.countTicketsGroupedByStatus();
        for (Object[] row : statusCounts) {
            TicketStatus status = (TicketStatus) row[0];
            Long count = (Long) row[1];
            ticketsByStatus.put(status.name(), count);
        }

        return DashboardStatsDto.builder()
                .totalTickets(totalTickets)
                .openTickets(openTickets)
                .inProgressTickets(inProgressTickets)
                .resolvedTickets(resolvedTickets)
                .closedTickets(closedTickets)
                .totalKnowledgeArticles(totalArticles)
                .publishedArticles(publishedArticles)
                .totalAgents(totalAgents)
                .totalCustomers(totalCustomers)
                .ticketsByStatus(ticketsByStatus)
                .build();
    }
}
