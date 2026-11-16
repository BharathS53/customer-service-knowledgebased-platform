package com.example.customerservice.service;

import com.example.customerservice.dto.CommentRequestDto;
import com.example.customerservice.dto.CommentResponseDto;
import com.example.customerservice.entity.SupportTicket;
import com.example.customerservice.entity.TicketComment;
import com.example.customerservice.entity.User;
import com.example.customerservice.repository.TicketCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketCommentServiceImpl implements TicketCommentService {

    private final TicketCommentRepository commentRepository;
    private final TicketService ticketService;
    private final UserService userService;

    @Override
    public CommentResponseDto addComment(Long ticketId, CommentRequestDto request, Long authorUserId) {
        SupportTicket ticket = ticketService.getTicketEntityById(ticketId);
        User author = userService.getUserEntityById(authorUserId);

        boolean isInternal = Boolean.TRUE.equals(request.getIsInternal());

        TicketComment comment = TicketComment.builder()
                .ticket(ticket)
                .user(author)
                .commentText(request.getCommentText().trim())
                .isInternal(isInternal)
                .build();

        TicketComment saved = commentRepository.save(comment);
        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsForTicket(Long ticketId, boolean includeInternal) {
        List<TicketComment> comments = includeInternal
                ? commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                : commentRepository.findByTicketIdAndIsInternalFalseOrderByCreatedAtAsc(ticketId);

        return comments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private CommentResponseDto mapToDto(TicketComment comment) {
        String primaryRole = comment.getUser().getRoles().stream()
                .findFirst()
                .map(r -> r.getName().name())
                .orElse("USER");

        return CommentResponseDto.builder()
                .id(comment.getId())
                .ticketId(comment.getTicket().getId())
                .userId(comment.getUser().getId())
                .userName(comment.getUser().getFullName())
                .userRole(primaryRole)
                .commentText(comment.getCommentText())
                .isInternal(comment.getIsInternal())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
