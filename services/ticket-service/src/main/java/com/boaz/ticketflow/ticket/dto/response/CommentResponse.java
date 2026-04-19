package com.boaz.ticketflow.ticket.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "A comment on a ticket")
public record CommentResponse(
        String id,
        String ticketId,
        String authorId,
        String content,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}