package com.camergo.notification.kafka;

import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.boaz.ticketflow.common.dtos.NotificationRequest;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationKafkaProducer {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "notifications";

    public void sendNotifications(List<NotificationRequest> requests) {
        // On peut envoyer chaque requête individuellement ou le lot entier
        // Pour plus de flexibilité, on envoie chaque requête séparément
        for (NotificationRequest request : requests) {
            kafkaTemplate.send(TOPIC, request.getRecipient(), request); // clé = destinataire (optionnel)
        }
    }
}