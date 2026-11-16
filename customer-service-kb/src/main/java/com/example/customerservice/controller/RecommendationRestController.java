package com.example.customerservice.controller;

import com.example.customerservice.dto.ArticleRecommendationDto;
import com.example.customerservice.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationRestController {

    private final RecommendationService recommendationService;

    /**
     * Recommended articles for a specific ticket (used by support agents).
     */
    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<List<ArticleRecommendationDto>> getRecommendationsForTicket(
            @PathVariable Long ticketId,
            @RequestParam(value = "limit", defaultValue = "5") int limit) {
        return ResponseEntity.ok(recommendationService.getRecommendationsForTicket(ticketId, limit));
    }

    /**
     * Live search recommendations / ticket deflection endpoint.
     */
    @GetMapping("/search")
    public ResponseEntity<List<ArticleRecommendationDto>> getRecommendationsByQuery(
            @RequestParam("query") String query,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "limit", defaultValue = "5") int limit) {
        return ResponseEntity.ok(recommendationService.getRecommendationsByQuery(query, categoryId, limit));
    }

    /**
     * Retrieves all available knowledge tags for filtering.
     */
    @GetMapping("/tags")
    public ResponseEntity<List<String>> getAllTags() {
        return ResponseEntity.ok(recommendationService.getAllTags());
    }
}
