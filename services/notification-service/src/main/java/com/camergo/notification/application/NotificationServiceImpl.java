package com.camergo.notification.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.boaz.ticketflow.common.domain.NotificationType;
import com.boaz.ticketflow.common.dtos.NotificationRequest;
import com.camergo.notification.channel.NotificationChannel;
import com.camergo.notification.kafka.NotificationKafkaProducer;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final List<NotificationChannel> channels;
    private final NotificationKafkaProducer kafkaProducer; // producteur Kafka

    @Override
    public void sendNotification(NotificationRequest request) {
        System.out.println("\n\nNotification type = " + request.getType() + "\n\n");
        NotificationChannel channel = findChannel(request.getType());
        channel.send(request);
    }

    @Override
    public void sendBulkNotifications(List<NotificationRequest> requests) {
        kafkaProducer.sendNotifications(requests); // publication dans Kafka
    }

    private NotificationChannel findChannel(NotificationType type) {
        return channels.stream()
            .filter(ch -> ch.getSupportedType() == type)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No channel for type: " + type)); // L'errerur est ici
    }
}