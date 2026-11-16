package com.example.customerservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentRequestDto {

    @NotBlank(message = "Comment text cannot be empty")
    private String commentText;

    private Boolean isInternal; // Defaults to false
}
