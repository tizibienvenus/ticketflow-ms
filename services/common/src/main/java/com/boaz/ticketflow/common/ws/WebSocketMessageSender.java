package com.boaz.ticketflow.common.ws;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketMessageSender {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Envoie un message à un utilisateur spécifique.
     *
     * @param userId  l'identifiant de l'utilisateur (doit correspondre au principal)
     * @param destination la destination (ex: "/queue/ride-request")
     * @param payload le contenu du message
     */
    public void sendToUser(String userId, String destination, Object payload) {
        messagingTemplate.convertAndSendToUser(userId, destination, payload);
        log.debug("Message envoyé à l'utilisateur {} sur {}", userId, destination);
    }

    /**
     * Envoie un message à tous les abonnés d'un topic.
     */
    public void sendToTopic(String topic, Object payload) {
        messagingTemplate.convertAndSend("/topic/" + topic, payload);
        log.debug("Message broadcast sur /topic/{}", topic);
    }

    /**
     * Envoie un message à tous les abonnés d'une destination (topic).
     */
    public void sendToAll(String destination, Object payload) {
        messagingTemplate.convertAndSend(destination, payload);
        log.debug("Message broadcast sur {}", destination);
    }
}