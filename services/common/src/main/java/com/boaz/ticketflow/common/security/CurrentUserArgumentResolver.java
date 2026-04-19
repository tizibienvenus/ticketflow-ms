package com.boaz.ticketflow.common.security;


import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;


/**
 * Resolves method parameters annotated with {@link CurrentUser}.
 * Extracts the {@code sub} claim from the Keycloak JWT and returns it as a {@link AuthenticatedUser}.
 */
@Slf4j
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class) &&
            (                
                AuthenticatedUser.class.isAssignableFrom(parameter.getParameterType())
            );
    }

    @Override
    public Object resolveArgument(
        MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            log.warn("@CurrentUser requested but no AuthenticatedUser found");
            return null;
        }

        Class<?> paramType = parameter.getParameterType();

        if (AuthenticatedUser.class.isAssignableFrom(paramType)) {
            return user;
        }

        return null;
    }
}