package com.boaz.ticketflow.ticket.service;

import com.boaz.ticketflow.ticket.dto.request.AddCommentRequest;
import com.boaz.ticketflow.ticket.dto.request.CreateTicketRequest;
import com.boaz.ticketflow.ticket.dto.request.UpdateTicketStatusRequest;
import com.boaz.ticketflow.ticket.dto.response.CommentResponse;
import com.boaz.ticketflow.ticket.dto.response.TicketResponse;
import com.boaz.ticketflow.ticket.dto.response.TicketSummaryResponse;
import com.boaz.ticketflow.ticket.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Contract for ticket management operations.
 * Implementations must handle persistence, external calls, and event publication.
 */
public interface TicketService {

    /**
     * Creates a new ticket and publishes a {@code ticket.created} Kafka event.
     *
     * @param request  creation payload
     * @param creatorId ID of the authenticated user making the request
     * @return the persisted ticket representation
     */
    TicketResponse createTicket(CreateTicketRequest request, String creatorId);

    /**
     * Returns a paginated list of all tickets, optionally filtered.
     *
     * @param status     optional status filter
     * @param creatorId  optional creator filter
     * @param assigneeId optional assignee filter
     * @param pageable   pagination and sorting parameters
     * @return page of ticket summaries
     */
    Page<TicketSummaryResponse> listTickets(
        TicketStatus status, 
        String creatorId, 
        String assigneeId, 
        Pageable pageable
    );

    /**
     * Returns the full detail of a single ticket including comments and linked documents.
     *
     * @param ticketId the ticket ID
     * @return ticket detail
     * @throws com.boaz.ticketflow.ticket.exception.TicketNotFoundException if not found
     */
    TicketResponse getTicketById(String ticketId);

    /**
     * Transitions a ticket's status according to the allowed lifecycle.
     * Publishes a {@code ticket.status.changed} Kafka event on success.
     *
     * @param ticketId  the ticket ID
     * @param request   the desired target status
     * @param changedBy ID of the authenticated user performing the change
     * @return updated ticket representation
     */
    TicketResponse updateTicketStatus(String ticketId, UpdateTicketStatusRequest request, String changedBy);

    /**
     * Adds a comment to an existing ticket.
     *
     * @param ticketId the ticket ID
     * @param request  comment payload
     * @param authorId ID of the authenticated user adding the comment
     * @return the persisted comment
     */
    CommentResponse addComment(
        String ticketId, 
        AddCommentRequest request, 
        String authorId
    );
}