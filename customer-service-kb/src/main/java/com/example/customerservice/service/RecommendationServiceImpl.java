package com.example.customerservice.service;

import com.example.customerservice.dto.ArticleRecommendationDto;
import com.example.customerservice.entity.ArticleStatus;
import com.example.customerservice.entity.ArticleTag;
import com.example.customerservice.entity.KnowledgeArticle;
import com.example.customerservice.entity.SupportTicket;
import com.example.customerservice.exception.ResourceNotFoundException;
import com.example.customerservice.repository.ArticleTagRepository;
import com.example.customerservice.repository.KnowledgeArticleRepository;
import com.example.customerservice.repository.SupportTicketRepository;
import com.example.customerservice.repository.TicketArticleMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {

    private final KnowledgeArticleRepository articleRepository;
    private final SupportTicketRepository ticketRepository;
    private final ArticleTagRepository tagRepository;
    private final TicketArticleMappingRepository mappingRepository;

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are",
            "aren't", "as", "at", "be", "because", "been", "before", "being", "below", "between", "both",
            "but", "by", "can", "cannot", "cant", "could", "did", "didn't", "do", "does", "doesn't",
            "doing", "don't", "down", "during", "each", "few", "for", "from", "further", "had", "has",
            "have", "having", "he", "her", "here", "hers", "herself", "him", "himself", "his", "how",
            "i", "if", "in", "into", "is", "isn't", "it", "its", "itself", "just", "me", "more", "most",
            "my", "myself", "no", "nor", "not", "now", "of", "off", "on", "once", "only", "or", "other",
            "our", "ours", "ourselves", "out", "over", "own", "please", "same", "she", "should", "so",
            "some", "such", "than", "that", "the", "their", "theirs", "them", "themselves", "then",
            "there", "these", "they", "this", "those", "through", "to", "too", "under", "until", "up",
            "very", "was", "we", "were", "what", "when", "where", "which", "while", "who", "whom",
            "why", "will", "with", "would", "you", "your", "yours", "yourself", "yourselves"
    );

    @Override
    public List<ArticleRecommendationDto> getRecommendationsForTicket(Long ticketId, int limit) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));

        Set<Long> linkedArticleIds = mappingRepository.findByTicketId(ticketId).stream()
                .map(m -> m.getArticle().getId())
                .collect(Collectors.toSet());

        Long categoryId = ticket.getCategory() != null ? ticket.getCategory().getId() : null;
        String combinedText = ticket.getIssueTitle() + " " + (ticket.getIssueDescription() != null ? ticket.getIssueDescription() : "");
        Set<String> keywords = extractKeywords(combinedText);

        List<KnowledgeArticle> publishedArticles = articleRepository.findByStatus(ArticleStatus.PUBLISHED);

        return publishedArticles.stream()
                .filter(article -> !linkedArticleIds.contains(article.getId())) // Filter out already linked solutions
                .map(article -> computeScore(article, keywords, categoryId))
                .filter(dto -> dto.getScore() > 0)
                .sorted(Comparator.comparingInt(ArticleRecommendationDto::getScore).reversed())
                .limit(limit > 0 ? limit : 5)
                .collect(Collectors.toList());
    }

    @Override
    public List<ArticleRecommendationDto> getRecommendationsByQuery(String query, Long categoryId, int limit) {
        if (query == null || query.trim().isBlank()) {
            return Collections.emptyList();
        }

        Set<String> keywords = extractKeywords(query);
        List<KnowledgeArticle> publishedArticles = articleRepository.findByStatus(ArticleStatus.PUBLISHED);

        return publishedArticles.stream()
                .map(article -> computeScore(article, keywords, categoryId))
                .filter(dto -> dto.getScore() > 0)
                .sorted(Comparator.comparingInt(ArticleRecommendationDto::getScore).reversed())
                .limit(limit > 0 ? limit : 5)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllTags() {
        return tagRepository.findAll().stream()
                .map(ArticleTag::getName)
                .sorted()
                .collect(Collectors.toList());
    }

    private ArticleRecommendationDto computeScore(KnowledgeArticle article, Set<String> keywords, Long targetCategoryId) {
        int score = 0;
        List<String> matchingReasons = new ArrayList<>();

        // 1. Category Alignment (Weight: 30%)
        if (targetCategoryId != null && article.getCategory() != null && targetCategoryId.equals(article.getCategory().getId())) {
            score += 30;
            matchingReasons.add("Category: " + article.getCategory().getName());
        }

        String titleLower = article.getTitle() != null ? article.getTitle().toLowerCase() : "";
        String descLower = article.getDescription() != null ? article.getDescription().toLowerCase() : "";
        String problemLower = article.getProblemStatement() != null ? article.getProblemStatement().toLowerCase() : "";
        String solutionLower = article.getStepByStepSolution() != null ? article.getStepByStepSolution().toLowerCase() : "";

        Set<String> articleTags = article.getTags() != null ?
                article.getTags().stream().map(t -> t.getName().toLowerCase()).collect(Collectors.toSet()) :
                Collections.emptySet();

        int titleHits = 0;
        int tagHits = 0;
        int problemHits = 0;
        int solutionHits = 0;

        for (String kw : keywords) {
            boolean matchedAny = false;

            // 2. Title Match (Weight: up to 40%)
            if (titleLower.contains(kw)) {
                titleHits++;
                matchedAny = true;
            }

            // 3. Tag Match (Weight: up to 30%)
            for (String tag : articleTags) {
                if (tag.contains(kw) || kw.contains(tag)) {
                    tagHits++;
                    if (!matchingReasons.contains("#" + tag)) {
                        matchingReasons.add("#" + tag);
                    }
                    matchedAny = true;
                }
            }

            // 4. Problem Statement Match (Weight: up to 20%)
            if (problemLower.contains(kw) || descLower.contains(kw)) {
                problemHits++;
                matchedAny = true;
            }

            // 5. Solution Match (Weight: up to 10%)
            if (solutionLower.contains(kw)) {
                solutionHits++;
                matchedAny = true;
            }

            if (matchedAny && !matchingReasons.contains(kw) && matchingReasons.size() < 6) {
                matchingReasons.add(kw);
            }
        }

        score += Math.min(titleHits, 2) * 20;
        score += Math.min(tagHits, 2) * 15;
        score += Math.min(problemHits, 2) * 10;
        score += Math.min(solutionHits, 1) * 5;

        // Cap score at 100
        int finalScore = Math.min(100, score);

        List<String> tagNames = article.getTags() != null ?
                article.getTags().stream().map(ArticleTag::getName).collect(Collectors.toList()) :
                Collections.emptyList();

        return ArticleRecommendationDto.builder()
                .articleId(article.getId())
                .title(article.getTitle())
                .description(article.getDescription())
                .categoryId(article.getCategory() != null ? article.getCategory().getId() : null)
                .categoryName(article.getCategory() != null ? article.getCategory().getName() : "General")
                .score(finalScore)
                .matchingKeywords(matchingReasons)
                .tags(tagNames)
                .problemStatement(article.getProblemStatement())
                .stepByStepSolution(article.getStepByStepSolution())
                .alreadyLinked(false)
                .build();
    }

    private Set<String> extractKeywords(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptySet();
        }
        String cleaned = text.toLowerCase().replaceAll("[^a-z0-9\\-]", " ");
        String[] tokens = cleaned.split("\\s+");
        Set<String> keywords = new LinkedHashSet<>();
        for (String token : tokens) {
            String trimmed = token.trim();
            if (trimmed.length() >= 3 && !STOP_WORDS.contains(trimmed)) {
                keywords.add(trimmed);
            }
        }
        return keywords;
    }
}
