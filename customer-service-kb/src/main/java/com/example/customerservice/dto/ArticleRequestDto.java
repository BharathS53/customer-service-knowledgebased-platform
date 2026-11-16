package com.example.customerservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleRequestDto {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotBlank(message = "Problem statement is required")
    private String problemStatement;

    @NotBlank(message = "Root cause is required")
    private String rootCause;

    @NotBlank(message = "Step-by-step solution is required")
    private String stepByStepSolution;

    private String status; // DRAFT, PUBLISHED, ARCHIVED

    private Set<String> tags;

    private String tagsInput; // Comma-separated tags string for form bindings

    public Set<String> getTags() {
        if ((tags == null || tags.isEmpty()) && tagsInput != null && !tagsInput.isBlank()) {
            Set<String> parsed = new java.util.HashSet<>();
            for (String part : tagsInput.split(",")) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    parsed.add(trimmed);
                }
            }
            return parsed;
        }
        return tags;
    }
}
