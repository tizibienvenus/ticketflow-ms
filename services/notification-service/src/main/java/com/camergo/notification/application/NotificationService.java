package com.camergo.notification.application;

import java.util.List;

import com.boaz.ticketflow.common.dtos.NotificationRequest;

public interface NotificationService {
    void sendNotification(NotificationRequest request);          // synchrone ou asynchrone selon configuration
    void sendBulkNotifications(List<NotificationRequest> requests); // toujours asynchrone via Kafka
}
