package com.example.customerservice.service;

import com.example.customerservice.dto.TicketAssignRequest;
import com.example.customerservice.dto.TicketDetailDto;
import com.example.customerservice.dto.TicketRequestDto;
import com.example.customerservice.dto.TicketResponseDto;
import com.example.customerservice.dto.TicketStatusUpdateRequest;
import com.example.customerservice.entity.SupportTicket;
import com.example.customerservice.entity.TicketPriority;
import com.example.customerservice.entity.TicketStatus;

import java.util.List;

public interface TicketService {
    TicketResponseDto createTicket(TicketRequestDto request, Long customerUserId);
    TicketResponseDto getTicketById(Long ticketId);
    TicketDetailDto getTicketDetails(Long ticketId, boolean includeInternalComments);
    TicketResponseDto getTicketByNumber(String ticketNumber);
    SupportTicket getTicketEntityById(Long ticketId);
    List<TicketResponseDto> getAllTickets();
    List<TicketResponseDto> getTicketsByCustomer(Long customerId);
    List<TicketResponseDto> getTicketsByAgent(Long agentId);
    List<TicketResponseDto> getTicketsByStatus(TicketStatus status);
    TicketResponseDto assignTicket(Long ticketId, TicketAssignRequest request, Long changedByUserId);
    TicketResponseDto updateTicketStatus(Long ticketId, TicketStatusUpdateRequest request, Long changedByUserId);
    void linkArticleToTicket(Long ticketId, Long articleId, Long linkedByUserId);
    void unlinkArticleFromTicket(Long ticketId, Long articleId);
}
