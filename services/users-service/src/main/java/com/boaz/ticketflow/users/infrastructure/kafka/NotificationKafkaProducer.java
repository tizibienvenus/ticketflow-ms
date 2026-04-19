package com.boaz.ticketflow.users.infrastructure.kafka;


import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.boaz.ticketflow.common.dtos.NotificationRequest;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationKafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "notifications";

    public void sendNotifications(NotificationRequest request) {
        kafkaTemplate.send(TOPIC, request.getRecipient(), request);
        System.err.println("Message moved to DLT after retries: " + request + " from topic: " + TOPIC);
    }
}