package com.camergo.notification.channel;

import com.boaz.ticketflow.common.domain.NotificationType;
import com.boaz.ticketflow.common.dtos.NotificationRequest;

public interface NotificationChannel {
    void send(NotificationRequest request);
    NotificationType getSupportedType();
}