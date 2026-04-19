package com.boaz.ticketflow.common.security;

import java.lang.annotation.*;

/**
 * Parameter-level annotation that injects the authenticated user
 * extracted from the Keycloak JWT {@code sub} claim.
 *
 * Usage:
 * <pre>
 *   public ResponseEntity<?> create(..., @CurrentUser AuthenticatedUser currentUser) { ... }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
}