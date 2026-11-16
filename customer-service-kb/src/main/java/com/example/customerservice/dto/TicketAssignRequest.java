package com.example.customerservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketAssignRequest {

    @NotNull(message = "Agent ID is required")
    private Long agentId;
}
