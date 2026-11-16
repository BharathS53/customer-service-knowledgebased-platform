package com.example.customerservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDetailDto {
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private List<CommentResponseDto> comments = new ArrayList<>();

    @Builder.Default
    private List<ArticleResponseDto> linkedArticles = new ArrayList<>();

    @Builder.Default
    private List<TicketHistoryDto> history = new ArrayList<>();
}
