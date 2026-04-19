

package com.boaz.ticketflow.common.ws;


import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import com.boaz.ticketflow.common.exceptions.InvalidTokenException;
import com.boaz.ticketflow.common.security.AuthenticatedUser;
import com.boaz.ticketflow.common.security.CustomPrincipal;
import com.boaz.ticketflow.common.security.JwtToCustomPrincipalConverter;
import com.google.auth.oauth2.JwtClaims;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; 


@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    
    private final JwtDecoder jwtDecoder;
    private final JwtToCustomPrincipalConverter jwtToCustomPrincipalConverter;
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
            message, StompHeaderAccessor.class);
        
        if (accessor == null) {
            return message;
        }
        
        StompCommand command = accessor.getCommand();
        
        if (null != command) 
        
        switch (command) {
            case CONNECT -> handleConnect(accessor);
            case SUBSCRIBE -> handleSubscribe(accessor);
            case DISCONNECT -> handleDisconnect(accessor);
            case SEND -> handleSend(accessor);
            default -> {
            }
        }
        
        return message;
    } 
    
    private void handleConnect(StompHeaderAccessor accessor) {
        String token = extractToken(accessor);
        
        if (token == null) {
            throw new SecurityException("Token JWT manquant");
        }

        try {
            // 1. Décoder et valider le JWT
            Jwt jwt = jwtDecoder.decode(token);

            // 2. Convertir en Authentication (avec CustomPrincipal)
            UsernamePasswordAuthenticationToken auth = jwtToCustomPrincipalConverter.convert(jwt);

            // 3. Placer l'authentification dans la session
            accessor.setUser(auth);

            AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

            log.info("Connexion WebSocket réussie : utilisateur {} (ID: {}), session {}",
                user.getUsername(), user.getId(), accessor.getSessionId());

        } catch (JwtException e) {
            log.error("Échec de la validation JWT : {}", e.getMessage());
            throw new SecurityException("Token JWT invalide");
        }
    }
    
    private void handleSubscribe(StompHeaderAccessor accessor) {
        Authentication auth = getAuthentication(accessor);
        String destination = accessor.getDestination();
        if (destination != null) {
            // Validation optionnelle des abonnements (selon votre logique métier)
            validateSubscription(destination, auth);
            log.debug("Abonnement autorisé : {} pour {}", destination, auth.getName());
        }
    }
    
    private void handleDisconnect(StompHeaderAccessor accessor) {
        log.debug("Déconnexion WebSocket : session {}", accessor.getSessionId());
    }

    private void handleSend(StompHeaderAccessor accessor) {
        Authentication auth = getAuthentication(accessor);
        String destination = accessor.getDestination();
        if (destination == null) {
            throw new IllegalArgumentException("La destination est requise");
        }
        validateSendPermission(destination, auth);
        log.debug("Message envoyé par {} vers {}", auth.getName(), destination);
    }

    private String extractToken(StompHeaderAccessor accessor) {
        // Chercher le token dans les headers
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authHeader = authHeaders.get(0);
            if (authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
            return authHeader;
        }
        
        // Chercher dans les query parameters (pour SockJS)
        List<String> cookieHeaders = accessor.getNativeHeader("cookie");

        if (cookieHeaders != null) {
            for (String cookie : cookieHeaders) {
                // parser le cookie si besoin
            }
        }

        
        return null;
    }
    
    
    private void validateSubscription(String destination, Authentication auth) {
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        String userIdStr = auth.getName(); // Nécessite que CustomPrincipal implémente Principal

        // Règles de validation par destination
        if (destination.startsWith("/topic/driver/")) {
            // Exemple : /topic/driver/{driverId}
            String driverId = extractIdFromDestination(destination, "/topic/driver/");
            if (!driverId.equals(userIdStr) && !isAdmin(user)) {
                throw new SecurityException("Accès interdit aux topics d'un autre conducteur");
            }
        } else if (destination.startsWith("/topic/city/")) {
            // Exemple : /topic/city/{cityId}
            String cityId = extractIdFromDestination(destination, "/topic/city/");
            // Récupérer la ville depuis les extras (à condition qu'elle soit présente)
            Object userCity = user.getExtras().get("city_id");
            if (userCity != null && !cityId.equals(userCity.toString()) && !isAdmin(user)) {
                throw new SecurityException("Accès interdit aux topics d'une autre ville");
            }
        } else if (destination.startsWith("/user/queue/")) {
            // Les queues personnelles sont gérées par Spring
            if (!destination.matches("/user/queue/[a-zA-Z0-9_-]+")) {
                throw new IllegalArgumentException("Format de destination invalide");
            }
        }
        // Autres destinations autorisées sans restriction
    }
    
    
    private String getConnectionType(StompHeaderAccessor accessor) {
        String userAgent = accessor.getFirstNativeHeader("User-Agent");
        if (userAgent != null) {
            if (userAgent.contains("Android") || userAgent.contains("iOS")) {
                return "MOBILE";
            } else if (userAgent.contains("Postman") || userAgent.contains("curl")) {
                return "TEST";
            }
        }
        return "WEB";
    }
    
    @Override
    public void afterSendCompletion(
        Message<?> message, 
        MessageChannel channel, 
        boolean sent, 
        Exception ex
    ) {
        if (ex != null) {
            log.error("Error sending WebSocket message: {}", ex.getMessage());
        }
    }

    private void validateSendPermission(String destination, Authentication auth) {
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        List<String> roles = user.getRoles();

        if (destination.startsWith("/app/driver/")) {
            // Vérifier que l'utilisateur a le rôle conducteur
            if (!roles.contains("ROLE_DRIVER")) {
                throw new SecurityException("Seuls les conducteurs peuvent envoyer des messages sur /app/driver/*");
            }
        } else if (destination.startsWith("/app/ride/")) {
            // Exemple : tout le monde peut envoyer des requêtes de course
            // Ajoutez vos propres règles
        }
    }

    private Authentication getAuthentication(StompHeaderAccessor accessor) {
        Principal principal = accessor.getUser();
        if (principal instanceof Authentication authentication) {
            return authentication;
        }
        throw new InvalidTokenException("Invalid authentication context");
    }


    private boolean isAdmin(AuthenticatedUser user) {
        return user.getRoles().contains("ROLE_ADMIN") || user.getRoles().contains("ROLE_ROOTADMIN");
    }

    private String extractIdFromDestination(String destination, String prefix) {
        return destination.substring(prefix.length());
    }

} 
