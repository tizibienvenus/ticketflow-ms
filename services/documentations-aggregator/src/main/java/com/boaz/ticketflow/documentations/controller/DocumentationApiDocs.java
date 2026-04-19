package com.boaz.ticketflow.documentations.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import com.boaz.ticketflow.documentations.config.ServiceDefinition;

import java.util.List;
import java.util.Map;

@Tag(name = "Documentation Aggregator", description = "APIs for aggregating OpenAPI specifications from multiple services")
public interface DocumentationApiDocs {

    @Operation(
        summary = "List all available services",
        description = "Returns a list of all services that have their OpenAPI documentation aggregated."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of services",
                     content = @Content(array = @ArraySchema(schema = @Schema(implementation = ServiceDefinition.class)))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<ServiceDefinition>> getServices();

    @Operation(
        summary = "Get OpenAPI specification for a specific service",
        description = "Fetches the aggregated OpenAPI (Swagger) specification for the given service. " +
                      "The service ID must match one of the services listed in the `/services` endpoint. " +
                      "The response is the raw OpenAPI JSON, with the 'servers' section rewritten to point to the documentation gateway."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OpenAPI specification",
                     content = @Content(schema = @Schema(implementation = Map.class),
            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                value = "{\"openapi\":\"3.0.1\",\"info\":{\"title\":\"Service API\",\"version\":\"1.0\"},\"servers\":[{\"url\":\"/\"}]}"))),
        @ApiResponse(responseCode = "404", description = "Service not found"),
        @ApiResponse(responseCode = "502", description = "Error fetching documentation from the downstream service")
    })
    ResponseEntity<Map<String, Object>> getServiceDocs(
        @Parameter(description = "Identifier of the service (e.g., 'identity-service', 'dispatch-service')", 
            example = "identity-service", required = true)
        @PathVariable String serviceId
    );

    @Operation(
        summary = "Swagger UI configuration",
        description = "Provides the configuration object for Swagger UI, listing all available services with their documentation URLs."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Swagger UI configuration",
            content = @Content(schema = @Schema(implementation = Map.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = "{\"configUrl\":\"/documentation/swagger-config\",\"urls\":[{\"name\":\"identity-service\",\"url\":\"/documentation/api-docs/identity-service\"}],\"validatorUrl\":\"none\"}")))
    })
    Map<String, Object> getSwaggerUiConfig();
}