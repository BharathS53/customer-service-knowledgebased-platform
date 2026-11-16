package com.example.customerservice.repository;

import com.example.customerservice.entity.SupportTicket;
import com.example.customerservice.entity.TicketPriority;
import com.example.customerservice.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    Optional<SupportTicket> findByTicketNumber(String ticketNumber);

    List<SupportTicket> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<SupportTicket> findByAssignedAgentIdOrderByCreatedAtDesc(Long agentId);

    List<SupportTicket> findByStatusOrderByCreatedAtDesc(TicketStatus status);

    List<SupportTicket> findByPriorityOrderByCreatedAtDesc(TicketPriority priority);

    long countByStatus(TicketStatus status);

    long countByAssignedAgentIdAndStatus(Long agentId, TicketStatus status);

    @Query("SELECT t.status, COUNT(t) FROM SupportTicket t GROUP BY t.status")
    List<Object[]> countTicketsGroupedByStatus();
}
