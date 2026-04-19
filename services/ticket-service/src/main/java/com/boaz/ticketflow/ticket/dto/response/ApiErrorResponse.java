package com.boaz.ticketflow.ticket.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.Map;

@Schema(description = "Standard error response")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(

    @Schema(description = "HTTP status code")
    int status,

    @Schema(description = "Short error code")
    String error,

    @Schema(description = "Human-readable error message")
    String message,

    @Schema(description = "Request path that caused the error")
    String path,

    @Schema(description = "Field-level validation errors")
    Map<String, String> fieldErrors,

    @Schema(description = "Timestamp of the error")
    OffsetDateTime timestamp
) {
    public static ApiErrorResponse of(int status, String error, String message, String path) {
    return new ApiErrorResponse(status, error, message, path, null, OffsetDateTime.now());
    }

    public static ApiErrorResponse withFieldErrors(
        int status, 
        String error, 
        String message, 
        String path,
        Map<String, String> fieldErrors
    ) {
        return new ApiErrorResponse(status, error, message, path, fieldErrors, OffsetDateTime.now());
    }
}