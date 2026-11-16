package com.example.customerservice.repository;

import com.example.customerservice.entity.ArticleStatus;
import com.example.customerservice.entity.KnowledgeArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {

    List<KnowledgeArticle> findByStatus(ArticleStatus status);

    List<KnowledgeArticle> findByCategoryIdAndStatus(Long categoryId, ArticleStatus status);

    @Query("SELECT DISTINCT a FROM KnowledgeArticle a " +
           "LEFT JOIN a.tags t " +
           "WHERE a.status = :status AND (" +
           "LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.problemStatement) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.stepByStepSolution) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<KnowledgeArticle> searchArticles(@Param("keyword") String keyword, @Param("status") ArticleStatus status);

    @Query("SELECT DISTINCT a FROM KnowledgeArticle a " +
           "LEFT JOIN a.tags t " +
           "WHERE a.status = 'PUBLISHED' AND (" +
           "LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.problemStatement) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.stepByStepSolution) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<KnowledgeArticle> searchPublishedArticles(@Param("keyword") String keyword);

    @Query("SELECT DISTINCT a FROM KnowledgeArticle a " +
           "JOIN a.tags t " +
           "WHERE a.status = 'PUBLISHED' AND LOWER(t.name) = LOWER(:tagName)")
    List<KnowledgeArticle> findPublishedByTagName(@Param("tagName") String tagName);

    long countByStatus(ArticleStatus status);
}
