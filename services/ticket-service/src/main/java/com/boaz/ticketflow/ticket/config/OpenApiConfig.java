package com.boaz.ticketflow.ticket.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

/**
 * OpenAPI / Swagger UI configuration.
 * Exposes bearer token auth so the Swagger UI can be used directly for manual testing.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Value("${server.port:8082}")
    private String serverPort;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                    new Server().url("http://localhost:" + serverPort).description("Local development"),
                    new Server().url("http://localhost:8080").description("Via API Gateway")
                ))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                    .addSecuritySchemes(SECURITY_SCHEME_NAME, bearerSecurityScheme())
                );
    }

    private Info apiInfo() {
        return new Info()
                .title("Ticket Service API")
                .description("""
                        TicketFlow - Support Ticket Management Microservice.
                        
                        **Authentication**: All endpoints require a valid Keycloak JWT.
                        Obtain a token via:
                        ```
                        POST http://localhost:8180/realms/ticketflow/protocol/openid-connect/token
                        ```
                        
                        **Scope-based access control (ABAC)**:
                        - `ticket:create` — Create a ticket
                        - `ticket:read`   — List / get tickets
                        - `ticket:update` — Change ticket status
                        - `ticket:comment` — Add comments
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("TIZI BIENVENUs")
                        .email("tizibienvenus@gmail.com"))
                .license(new License().name("Apache 2.0").url("http://www.apache.org/licenses/LICENSE-2.0"));
    }

    private SecurityScheme bearerSecurityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Paste your Keycloak access_token here");
    }
}