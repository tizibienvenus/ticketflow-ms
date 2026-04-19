package com.boaz.ticketflow.ticket.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boaz.ticketflow.common.security.AuthenticatedUser;
import com.boaz.ticketflow.common.security.CurrentUser;
import com.boaz.ticketflow.ticket.dto.request.AddCommentRequest;
import com.boaz.ticketflow.ticket.dto.request.CreateTicketRequest;
import com.boaz.ticketflow.ticket.dto.request.UpdateTicketStatusRequest;
import com.boaz.ticketflow.ticket.dto.response.CommentResponse;
import com.boaz.ticketflow.ticket.dto.response.TicketResponse;
import com.boaz.ticketflow.ticket.dto.response.TicketSummaryResponse;
import com.boaz.ticketflow.ticket.enums.TicketStatus;
import com.boaz.ticketflow.ticket.service.TicketService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController implements TicketControllerDoc {

    private final TicketService ticketService;

    @Override
    @PostMapping
    @PreAuthorize("hasAuthority('ticket:create')")
    public ResponseEntity<TicketResponse> createTicket(
        @Valid @RequestBody CreateTicketRequest request,
        @CurrentUser AuthenticatedUser currentUser) {

        TicketResponse response = ticketService.createTicket(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @GetMapping
    @PreAuthorize("hasAuthority('ticket:read')")
    public ResponseEntity<Page<TicketSummaryResponse>> listTickets(
        TicketStatus status,
        String creatorId,
        String assigneeId,
        Pageable pageable) {

        return ResponseEntity.ok(ticketService.listTickets(status, creatorId, assigneeId, pageable));
    }

    @Override
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ticket:read')")
    public ResponseEntity<TicketResponse> getTicketById(String id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    @Override
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ticket:update')")
    public ResponseEntity<TicketResponse> updateStatus(
        String id,
        @Valid @RequestBody UpdateTicketStatusRequest request,
        @CurrentUser AuthenticatedUser currentUser) {

        return ResponseEntity.ok(ticketService.updateTicketStatus(id, request, currentUser.getId()));
    }
    
    @Override
    @PostMapping("/{id}/comments")
    @PreAuthorize("hasAuthority('ticket:comment')")
    public ResponseEntity<CommentResponse> addComment(
        String id,
        @Valid @RequestBody AddCommentRequest request,
        @CurrentUser AuthenticatedUser currentUser) {

        CommentResponse comment = ticketService.addComment(id, request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }
}