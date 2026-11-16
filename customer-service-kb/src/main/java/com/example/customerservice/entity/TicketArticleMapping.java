package com.example.customerservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_article_mapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"ticket", "article", "linkedBy"})
public class TicketArticleMapping {

    @EmbeddedId
    private TicketArticleMappingId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ticketId")
    @JoinColumn(name = "ticket_id", nullable = false)
    private SupportTicket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("articleId")
    @JoinColumn(name = "article_id", nullable = false)
    private KnowledgeArticle article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_by", nullable = false)
    private User linkedBy;

    @CreationTimestamp
    @Column(name = "linked_at", updatable = false)
    private LocalDateTime linkedAt;
}
