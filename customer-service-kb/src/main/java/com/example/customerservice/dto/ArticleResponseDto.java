package com.example.customerservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleResponseDto {
    private Long id;
    private String title;
    private String description;
    private Long categoryId;
    private String categoryName;
    private String problemStatement;
    private String rootCause;
    private String stepByStepSolution;
    private String status;
    private Long createdById;
    private String createdByName;
    private Set<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
