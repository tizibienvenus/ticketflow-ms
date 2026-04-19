package com.boaz.ticketflow.ticket.service.impl;

import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boaz.ticketflow.ticket.controller.TicketMapper;
import com.boaz.ticketflow.ticket.dto.request.AddCommentRequest;
import com.boaz.ticketflow.ticket.dto.request.CreateTicketRequest;
import com.boaz.ticketflow.ticket.dto.request.UpdateTicketStatusRequest;
import com.boaz.ticketflow.ticket.dto.response.CommentResponse;
import com.boaz.ticketflow.ticket.dto.response.TicketResponse;
import com.boaz.ticketflow.ticket.dto.response.TicketSummaryResponse;
import com.boaz.ticketflow.ticket.entity.Ticket;
import com.boaz.ticketflow.ticket.entity.TicketComment;
import com.boaz.ticketflow.ticket.enums.TicketStatus;
import com.boaz.ticketflow.ticket.event.TicketCreatedEvent;
import com.boaz.ticketflow.ticket.event.TicketStatusChangedEvent;
import com.boaz.ticketflow.ticket.exception.AssigneeValidationException;
import com.boaz.ticketflow.ticket.exception.InvalidStatusTransitionException;
import com.boaz.ticketflow.ticket.exception.TicketNotFoundException;
import com.boaz.ticketflow.ticket.kafka.TicketEventPublisher;
import com.boaz.ticketflow.ticket.repository.TicketRepository;
import com.boaz.ticketflow.ticket.service.TicketService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

//import org.springframework.retry.annotation.CircuitBreaker;

/**
 * Core business logic for ticket lifecycle management.
 *
 * Responsibilities:
 * - Validate assignees via user-service (Feign + Resilience4j)
 * - Persist tickets and comments via JPA
 * - Publish domain events to Kafka on state changes
 *
 * Follows SOLID: Single responsibility per method, open for extension via interface.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    //private final UserServiceClient userServiceClient;
    private final TicketEventPublisher eventPublisher;

    // ── Create ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request, String creatorId) {
        log.info("Creating ticket for creator={} title='{}'", creatorId, request.title());

        if (request.assigneeId() != null) {
            validateAssignee(request.assigneeId());
        }

        Ticket ticket = Ticket.builder()
                .title(request.title())
                .description(request.description())
                .priority(request.priority())
                .status(TicketStatus.OPEN)
                .creatorId(creatorId)
                .assigneeId(request.assigneeId())
                .build();

        Ticket saved = ticketRepository.save(ticket);
        log.info("Ticket created: id={}", saved.getId());

        eventPublisher.publishTicketCreated(new TicketCreatedEvent(
                saved.getId(),
                saved.getTitle(),
                saved.getStatus(),
                saved.getPriority(),
                saved.getCreatorId(),
                saved.getAssigneeId(),
                saved.getCreatedAt()
        ));

        return ticketMapper.toResponse(saved);
    }

    // ── Read ────────────────────────────────────────────────────────────────

    @Override
    public Page<TicketSummaryResponse> listTickets(
        TicketStatus status, 
        String creatorId,
        String assigneeId, 
        Pageable pageable
    ) {
        log.debug("Listing tickets with filters: status={}, creatorId={}, assigneeId={}",
                status, creatorId, assigneeId);

        return ticketRepository
                .findAllWithFilters(status, creatorId, assigneeId, pageable)
                .map(ticketMapper::toSummary);
    }

    @Override
    public TicketResponse getTicketById(String ticketId) {
        log.debug("Fetching ticket: id={}", ticketId);
        Ticket ticket = findTicketOrThrow(ticketId);
        return ticketMapper.toResponse(ticket);
    }

    // ── Status transition ───────────────────────────────────────────────────

    @Override
    @Transactional
    public TicketResponse updateTicketStatus(
        String ticketId, 
        UpdateTicketStatusRequest request, 
        String changedBy
    ) {
        log.info("Status change request: ticketId={} → {} by userId={}", ticketId, request.status(), changedBy);

        Ticket ticket = findTicketOrThrow(ticketId);
        TicketStatus previousStatus = ticket.getStatus();

        if (!previousStatus.canTransitionTo(request.status())) {
            throw new InvalidStatusTransitionException(previousStatus, request.status());
        }

        ticket.updateStatus(request.status());
        Ticket saved = ticketRepository.save(ticket);

        eventPublisher.publishTicketStatusChanged(new TicketStatusChangedEvent(
                saved.getId(),
                saved.getTitle(),
                previousStatus,
                saved.getStatus(),
                changedBy,
                saved.getCreatorId(),
                saved.getAssigneeId(),
                OffsetDateTime.now()
        ));

        log.info("Ticket {} status changed: {} → {}", ticketId, previousStatus, saved.getStatus());
        return ticketMapper.toResponse(saved);
    }

    // ── Comments ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CommentResponse addComment(String ticketId, AddCommentRequest request, String authorId) {
        log.info("Adding comment to ticketId={} by authorId={}", ticketId, authorId);

        Ticket ticket = findTicketOrThrow(ticketId);

        TicketComment comment = TicketComment.builder()
                .authorId(authorId)
                .content(request.content())
                .build();

        ticket.addComment(comment);
        ticketRepository.save(ticket);

        log.info("Comment added: commentId={} on ticketId={}", comment.getId(), ticketId);
        return ticketMapper.toCommentResponse(comment);
    }

    // ── Private helpers ─────────────────────────────────────────────────────

    private Ticket findTicketOrThrow(String ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
    }

    /**
     * Validates that the given assigneeId corresponds to an existing user.
     * Protected by CircuitBreaker + Retry via Resilience4j annotations.
     * If user-service is down, fallback returns null and we throw AssigneeValidationException.
     */
    @CircuitBreaker(name = "user-service", fallbackMethod = "assigneeFallback")
    @Retry(name = "user-service")
    private void validateAssignee(String assigneeId) {
        log.debug("Validating assignee: userId={}", assigneeId);
        /*UserResponse user = userServiceClient.getUserById(assigneeId);
        if (user == null) {
            throw new AssigneeValidationException(assigneeId);
        }*/
        //log.debug("Assignee validated: userId={} name={} {}", assigneeId, user.firstName(), user.lastName());
    }

    @SuppressWarnings("unused")
    private void assigneeFallback(String assigneeId, Throwable cause) {
        log.warn("[CB FALLBACK] user-service circuit open for assigneeId={}: {}", assigneeId, cause.getMessage());
        throw new AssigneeValidationException(assigneeId, cause);
    }
}