package com.boaz.ticketflow.documentations.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boaz.ticketflow.documentations.services.DocumentationGuideService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/documentation")
@Tag(name = "Documentation Guide", description = "Human-readable guide for the documentation aggregator")
public class DocumentationGuideController {

    private static final MediaType MARKDOWN = MediaType.parseMediaType("text/markdown");

    private final DocumentationGuideService documentationGuideService;

    public DocumentationGuideController(DocumentationGuideService documentationGuideService) {
        this.documentationGuideService = documentationGuideService;
    }

    @Operation(
        summary = "Get the documentation guide in Markdown",
        description = "Returns the Markdown guide used to describe the documentation aggregator in Swagger UI and Scalar."
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
