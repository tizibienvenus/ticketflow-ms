package com.camergo.notification.kafka;

import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import com.boaz.ticketflow.common.dtos.NotificationRequest;
import com.camergo.notification.application.NotificationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationKafkaConsumer {
    private final NotificationService notificationService; // Attention : ici on utilise la méthode synchrone

    @RetryableTopic(
        attempts = "4",
        backoff = @Backoff(delay = 1000, multiplier = 2.0),
        dltTopicSuffix = "-dlt"
    )
    @KafkaListener(topics = "notifications", groupId = "notification-group")
    public void consume(NotificationRequest request) {
        notificationService.sendNotification(request); // appel synchrone individuel
    }

    @DltHandler
    public void handleDlt(NotificationRequest request, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        // Log ou alerter sur les messages qui ont échoué après tous les retries
        System.err.println("Message moved to DLT after retries: " + request + " from topic: " + topic);
    }
}