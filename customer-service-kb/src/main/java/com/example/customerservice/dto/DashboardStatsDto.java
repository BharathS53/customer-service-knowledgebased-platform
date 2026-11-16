package com.example.customerservice.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDto {
    private long totalTickets;
    private long openTickets;
    private long inProgressTickets;
    private long resolvedTickets;
    private long closedTickets;
    private long totalKnowledgeArticles;
    private long publishedArticles;
    private long totalAgents;
    private long totalCustomers;
    private Map<String, Long> ticketsByStatus;
}
