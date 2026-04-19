package com.boaz.ticketflow.ticket.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.boaz.ticketflow.common.security.AuthenticatedUser;
import com.boaz.ticketflow.common.security.CurrentUser;
import com.boaz.ticketflow.ticket.dto.request.AddCommentRequest;
import com.boaz.ticketflow.ticket.dto.request.CreateTicketRequest;
import com.boaz.ticketflow.ticket.dto.request.UpdateTicketStatusRequest;
import com.boaz.ticketflow.ticket.dto.response.ApiErrorResponse;
import com.boaz.ticketflow.ticket.dto.response.CommentResponse;
import com.boaz.ticketflow.ticket.dto.response.TicketResponse;
import com.boaz.ticketflow.ticket.dto.response.TicketSummaryResponse;
import com.boaz.ticketflow.ticket.enums.TicketStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST controller for ticket lifecycle management.
 *
 * All endpoints are secured with scope-based authorization (ABAC).
 * Roles are never used directly — only scopes from the JWT claim are checked.
 */
@Tag(name = "Tickets", description = "Support ticket lifecycle management")
@SecurityRequirement(name = "bearerAuth")
public interface TicketControllerDoc {

    // ── POST /api/tickets ─────────────────────────────────────────────────

    @Operation(
            summary = "Create a new support ticket",
            description = "Creates a ticket with status OPEN. Publishes a ticket.created Kafka event."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ticket created successfully",
                    content = @Content(schema = @Schema(implementation = TicketResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions — requires ticket:create"),
            @ApiResponse(responseCode = "502", description = "Assignee validation failed — user-service unreachable",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody CreateTicketRequest request,
            @Parameter(hidden = true) @CurrentUser AuthenticatedUser currentUser);

    // ── GET /api/tickets ──────────────────────────────────────────────────

    @Operation(
            summary = "List tickets (paginated)",
            description = "Returns a paginated list of tickets. Supports optional filtering by status, creatorId, assigneeId."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of tickets returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions — requires ticket:read")
    })
    ResponseEntity<Page<TicketSummaryResponse>> listTickets(
            @Parameter(description = "Filter by ticket status")
            @RequestParam(required = false) TicketStatus status,

            @Parameter(description = "Filter by creator id")
            @RequestParam(required = false) String creatorId,

            @Parameter(description = "Filter by assignee id")
            @RequestParam(required = false) String assigneeId,

            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable);

    // ── GET /api/tickets/{id} ─────────────────────────────────────────────    
    @Operation(
            summary = "Get ticket detail",
            description = "Returns the full ticket including comments and linked document IDs."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket found",
                    content = @Content(schema = @Schema(implementation = TicketResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions — requires ticket:read"),
            @ApiResponse(responseCode = "404", description = "Ticket not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    ResponseEntity<TicketResponse> getTicketById(
            @Parameter(description = "Ticket id") @PathVariable String id);

    // ── PATCH /api/tickets/{id}/status ────────────────────────────────────
    @Operation(
            summary = "Change ticket status",
            description = "Transitions ticket status. Allowed flow: OPEN → IN_PROGRESS → RESOLVED → CLOSED. " +
                    "Publishes a ticket.status.changed Kafka event."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated",
                    content = @Content(schema = @Schema(implementation = TicketResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions — requires ticket:update"),
            @ApiResponse(responseCode = "404", description = "Ticket not found"),
            @ApiResponse(responseCode = "409", description = "Invalid status transition",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    ResponseEntity<TicketResponse> updateStatus(
            @Parameter(description = "Ticket id") @PathVariable String id,
            @Valid @RequestBody UpdateTicketStatusRequest request,
            @Parameter(hidden = true) @CurrentUser AuthenticatedUser currentUserId);

    // ── POST /api/tickets/{id}/comments ──────────────────────────────────


    @Operation(
            summary = "Add a comment to a ticket",
            description = "Adds a timestamped comment attributed to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Comment added",
                content = @Content(schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions — requires ticket:comment"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    ResponseEntity<CommentResponse> addComment(
            @Parameter(description = "Ticket id") @PathVariable String id,
            @Valid @RequestBody AddCommentRequest request,
            @Parameter(hidden = true) @CurrentUser AuthenticatedUser currentUserId);
}