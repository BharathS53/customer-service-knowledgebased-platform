package com.example.customerservice.service;

import com.example.customerservice.dto.ArticleRecommendationDto;

import java.util.List;

public interface RecommendationService {

    /**
     * Recommends published Knowledge Base articles for a given support ticket
     * based on keyword matching, category alignment, and tag relevance.
     * Excludes articles that are already linked to the ticket.
     *
     * @param ticketId The ID of the support ticket
     * @param limit    Maximum number of recommendations to return
     * @return Ranked list of article recommendations with relevance scores
     */
    List<ArticleRecommendationDto> getRecommendationsForTicket(Long ticketId, int limit);

    /**
     * Recommends published Knowledge Base articles based on free-form search query
     * and optional category filter (used for live self-service deflection).
     *
     * @param query      User input text (issue title, search phrase)
     * @param categoryId Optional category ID
     * @param limit      Maximum number of recommendations to return
     * @return Ranked list of article recommendations
     */
    List<ArticleRecommendationDto> getRecommendationsByQuery(String query, Long categoryId, int limit);

    /**
     * Retrieves all unique tags available across the knowledge base.
     *
     * @return List of tag names
     */
    List<String> getAllTags();
}
