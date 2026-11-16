package com.example.customerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO representing an intelligently matched Knowledge Base Article recommendation
 * with relevance scoring, category alignment, and matching keywords breakdown.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleRecommendationDto {
    private Long articleId;
    private String title;
    private String description;
    private Long categoryId;
    private String categoryName;
    private int score; // Relevance score between 1 and 100
    private List<String> matchingKeywords;
    private List<String> tags;
    private String problemStatement;
    private String stepByStepSolution;
    private boolean alreadyLinked;
}
