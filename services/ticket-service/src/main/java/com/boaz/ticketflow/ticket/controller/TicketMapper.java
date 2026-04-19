package com.boaz.ticketflow.ticket.controller;

import com.boaz.ticketflow.ticket.dto.response.CommentResponse;
import com.boaz.ticketflow.ticket.dto.response.TicketResponse;
import com.boaz.ticketflow.ticket.dto.response.TicketSummaryResponse;
import com.boaz.ticketflow.ticket.entity.Ticket;
import com.boaz.ticketflow.ticket.entity.TicketComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Ticket domain objects.
 * All mappings are explicit — no magic field inference across layers.
 */
@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(target = "comments", source = "comments")
    @Mapping(target = "documentIds", source = "documentIds")
    TicketResponse toResponse(Ticket ticket);

    @Mapping(target = "commentCount", expression = "java(ticket.getComments().size())")
    TicketSummaryResponse toSummary(Ticket ticket);

    @Mapping(target = "ticketId", source = "ticket.id")
    CommentResponse toCommentResponse(TicketComment comment);
}