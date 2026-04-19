package com.camergo.notification.interfaces;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.camergo.notification.documentation.NotificationDocumentationGuideService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/documentation")
@Tag(name = "Notification Documentation", description = "Human-readable Markdown guide for notification-service")
public class NotificationDocumentationController {

    private static final MediaType MARKDOWN = MediaType.parseMediaType("text/markdown");

    private final NotificationDocumentationGuideService documentationGuideService;

    public NotificationDocumentationController(
        NotificationDocumentationGuideService documentationGuideService
    ) {
        this.documentationGuideService = documentationGuideService;
    }

    @Operation(
        summary = "Get the notification-service guide in Markdown",
        description = "Returns the Markdown guide embedded in the OpenAPI description of notification-service."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Markdown guide",
        content = @Content(mediaType = "text/markdown")
    )
    @GetMapping(value = "/guide.md", produces = "text/markdown")
    public ResponseEntity<String> getGuideMarkdown() {
        return ResponseEntity
            .ok()
            .contentType(MARKDOWN)
            .body(documentationGuideService.getGuideMarkdown());
    }
}
