package com.example.customerservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketHistoryDto {
    private Long id;
    private Long ticketId;
    private Long changedById;
    private String changedByName;
    private String fieldChanged;
    private String oldValue;
    private String newValue;
    private String changeDescription;
    private LocalDateTime createdAt;
}
