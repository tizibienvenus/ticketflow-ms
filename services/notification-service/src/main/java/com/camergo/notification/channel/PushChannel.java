package com.camergo.notification.channel;

import org.springframework.stereotype.Component;

import com.boaz.ticketflow.common.domain.NotificationType;
import com.boaz.ticketflow.common.dtos.NotificationRequest;
import com.boaz.ticketflow.common.dtos.PushRequest;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PushChannel implements NotificationChannel {

    private final FirebaseMessaging firebaseMessaging;

    @Override
    public void send(NotificationRequest request) {
        PushRequest pushReq = (PushRequest) request;
        Message message = Message.builder()
                .setToken(pushReq.getRecipient())  // token FCM du destinataire
                .setNotification(Notification.builder()
                        .setTitle(pushReq.getTitle())
                        .setBody(pushReq.getBody())
                        .setImage(pushReq.getImageUrl())
                        .build())
                .putAllData(pushReq.getData())
                .build();
        firebaseMessaging.sendAsync(message); // asynchrone
    }

    @Override
    public NotificationType getSupportedType() { return NotificationType.PUSH; }
}