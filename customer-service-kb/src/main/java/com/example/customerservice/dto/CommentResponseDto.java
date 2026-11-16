package com.example.customerservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponseDto {
    private Long id;
    private Long ticketId;
    private Long userId;
    private String userName;
    private String userRole;
    private String commentText;
    private Boolean isInternal;
    private LocalDateTime createdAt;
}
