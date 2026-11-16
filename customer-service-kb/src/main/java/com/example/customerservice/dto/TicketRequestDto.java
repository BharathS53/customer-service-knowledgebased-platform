package com.example.customerservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketRequestDto {

    @NotBlank(message = "Issue title is required")
    private String issueTitle;

    @NotBlank(message = "Issue description is required")
    private String issueDescription;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private String priority; // LOW, MEDIUM, HIGH, CRITICAL (default: MEDIUM)
}
