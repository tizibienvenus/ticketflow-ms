package com.boaz.ticketflow.common.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign interceptor that propagates the Authorization header (Bearer JWT)
 * from the incoming request to all outgoing Feign calls.
 *
 * This satisfies: SEC-004 - Propagation JWT entre services.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class FeignJwtRequestInterceptor implements RequestInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            log.debug("No request context available for Feign JWT propagation");
            return;
        }

        HttpServletRequest request = attributes.getRequest();
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            template.header(AUTHORIZATION_HEADER, authorizationHeader);
            log.debug("JWT propagated to downstream Feign call: {}", template.url());
        } else {
            log.warn(
                "No Bearer token found in current request context — Feign call to {} will be unauthenticated",
                template.url()
            );
        }
    }
}