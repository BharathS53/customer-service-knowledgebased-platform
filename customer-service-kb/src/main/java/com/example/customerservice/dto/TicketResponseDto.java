package com.example.customerservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponseDto {
    private Long id;
    private String ticketNumber;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private Long assignedAgentId;
    private String assignedAgentName;
    private Long categoryId;
    private String categoryName;
    private String issueTitle;
    private String issueDescription;
    private String priority;
    private String status;
    private String resolutionDetails;
    private int commentsCount;
    private int linkedArticlesCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
