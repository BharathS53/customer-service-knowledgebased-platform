package com.example.customerservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketStatusUpdateRequest {

    @NotBlank(message = "Status is required")
    private String status; // OPEN, IN_PROGRESS, WAITING, RESOLVED, CLOSED

    private String resolutionDetails;

    private String note;
}
