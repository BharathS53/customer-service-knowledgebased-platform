package com.example.customerservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketArticleMappingId implements Serializable {

    @Column(name = "ticket_id")
    private Long ticketId;

    @Column(name = "article_id")
    private Long articleId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TicketArticleMappingId that = (TicketArticleMappingId) o;
        return Objects.equals(ticketId, that.ticketId) &&
               Objects.equals(articleId, that.articleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticketId, articleId);
    }
}
