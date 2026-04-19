package com.boaz.ticketflow.ticket.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for adding a comment to a ticket")
public record AddCommentRequest(

    @NotBlank(message = "Comment content is required")
    @Size(min = 1, max = 5000, message = "Content must be between 1 and 5000 characters")
    @Schema(description = "The comment text")
    String content

) {}