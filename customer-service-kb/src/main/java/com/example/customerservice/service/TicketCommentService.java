package com.example.customerservice.service;

import com.example.customerservice.dto.CommentRequestDto;
import com.example.customerservice.dto.CommentResponseDto;

import java.util.List;

public interface TicketCommentService {
    CommentResponseDto addComment(Long ticketId, CommentRequestDto request, Long authorUserId);
    List<CommentResponseDto> getCommentsForTicket(Long ticketId, boolean includeInternal);
}
