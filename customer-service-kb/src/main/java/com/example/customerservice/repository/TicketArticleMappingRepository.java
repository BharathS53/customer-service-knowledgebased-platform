package com.example.customerservice.repository;

import com.example.customerservice.entity.TicketArticleMapping;
import com.example.customerservice.entity.TicketArticleMappingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketArticleMappingRepository extends JpaRepository<TicketArticleMapping, TicketArticleMappingId> {
    List<TicketArticleMapping> findByTicketId(Long ticketId);
    List<TicketArticleMapping> findByArticleId(Long articleId);
    boolean existsByTicketIdAndArticleId(Long ticketId, Long articleId);
}
