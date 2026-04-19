package com.boaz.ticketflow.ticket.mock;

import com.boaz.ticketflow.ticket.dto.request.AddCommentRequest;
import com.boaz.ticketflow.ticket.dto.request.CreateTicketRequest;
import com.boaz.ticketflow.ticket.dto.request.UpdateTicketStatusRequest;
import com.boaz.ticketflow.ticket.dto.response.CommentResponse;
import com.boaz.ticketflow.ticket.dto.response.TicketResponse;
import com.boaz.ticketflow.ticket.dto.response.TicketSummaryResponse;
import com.boaz.ticketflow.ticket.entity.Ticket;
import com.boaz.ticketflow.ticket.entity.TicketComment;
import com.boaz.ticketflow.ticket.enums.TicketPriority;
import com.boaz.ticketflow.ticket.enums.TicketStatus;

import java.time.OffsetDateTime;
import java.util.Collections;

/**
 * Centralized test fixtures — no duplication across test classes.
 * All IDs are fixed for deterministic assertions.
 */
public final class TicketTestFixtures {

    private TicketTestFixtures() {}

    public static final String TICKET_ID    = "11111111-1111-1111-1111-111111111111";
    public static final String CREATOR_ID   = "22222222-2222-2222-2222-222222222222";
    public static final String ASSIGNEE_ID  = "33333333-3333-3333-3333-333333333333";
    public static final String COMMENT_ID   = "44444444-4444-4444-4444-444444444444";

    public static CreateTicketRequest createTicketRequest() {
        return new CreateTicketRequest(
            "Login broken on Safari",
            "Users cannot log in using Safari 17 on macOS Sonoma. Steps to reproduce: open Safari, navigate to /login.",
            TicketPriority.HIGH,
            null
        );
    }

    public static CreateTicketRequest createTicketRequestWithAssignee() {
        return new CreateTicketRequest(
            "Login broken on Safari",
            "Users cannot log in using Safari 17 on macOS Sonoma.",
            TicketPriority.HIGH,
            ASSIGNEE_ID
        );
    }

    public static UpdateTicketStatusRequest statusRequest(TicketStatus status) {
        return new UpdateTicketStatusRequest(status);
    }

    public static AddCommentRequest commentRequest() {
        return new AddCommentRequest("This is a test comment with enough content.");
    }

    public static Ticket openTicket() {
        return Ticket.builder()
            .title("Login broken on Safari")
            .description("Users cannot log in using Safari 17.")
            .status(TicketStatus.OPEN)
            .priority(TicketPriority.HIGH)
            .creatorId(CREATOR_ID)
            .assigneeId(null)
            .createdAt(OffsetDateTime.now())
            .updatedAt(OffsetDateTime.now())
            .version(0L)
            .build();
    }

    public static Ticket ticketWithStatus(TicketStatus status) {
        Ticket ticket = openTicket();
        ticket.setStatus(status);
        return ticket;
    }

    public static TicketComment comment(Ticket ticket) {
        return TicketComment.builder()
            .ticket(ticket)
            .authorId(CREATOR_ID)
            .content("This is a test comment with enough content.")
            .createdAt(OffsetDateTime.now())
            .updatedAt(OffsetDateTime.now())
            .build();
    }

    public static TicketResponse ticketResponse(Ticket ticket) {
        return new TicketResponse(
            ticket.getId(),
            ticket.getTitle(),
            ticket.getDescription(),
            ticket.getStatus(),
            ticket.getPriority(),
            ticket.getCreatorId(),
            ticket.getAssigneeId(),
            ticket.getCreatedAt(),
            ticket.getUpdatedAt(),
            ticket.getResolvedAt(),
            ticket.getClosedAt(),
            Collections.emptyList(),
            Collections.emptySet()
        );
    }

    public static TicketSummaryResponse ticketSummaryResponse(Ticket ticket) {
        return new TicketSummaryResponse(
            ticket.getId(),
            ticket.getTitle(),
            ticket.getStatus(),
            ticket.getPriority(),
            ticket.getCreatorId(),
            ticket.getAssigneeId(),
            ticket.getCreatedAt(),
            ticket.getUpdatedAt(),
            0
        );
    }

    public static CommentResponse commentResponse(TicketComment comment) {
        return new CommentResponse(
            comment.getId(),
            comment.getTicket().getId(),
            comment.getAuthorId(),
            comment.getContent(),
            comment.getCreatedAt(),
            comment.getUpdatedAt()
        );
    }
}