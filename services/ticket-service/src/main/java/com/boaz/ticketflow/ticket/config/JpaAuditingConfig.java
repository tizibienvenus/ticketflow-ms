package com.boaz.ticketflow.ticket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables JPA auditing so @CreatedDate and @LastModifiedDate on entities are
 * populated automatically.
 *
 * Kept separate from SecurityConfig intentionally - single responsibility.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}