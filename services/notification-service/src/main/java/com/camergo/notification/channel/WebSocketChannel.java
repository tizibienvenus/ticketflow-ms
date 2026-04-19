package com.camergo.notification.channel;

import org.springframework.stereotype.Component;

import com.boaz.ticketflow.common.domain.NotificationType;
import com.boaz.ticketflow.common.dtos.NotificationRequest;
import com.boaz.ticketflow.common.dtos.WebSocketRequest;
import com.boaz.ticketflow.common.ws.WebSocketMessageSender;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebSocketChannel implements NotificationChannel {
    
    private final WebSocketMessageSender webSocketSender;

    @Override
    public void send(NotificationRequest request) {
        WebSocketRequest wsReq = (WebSocketRequest) request;
        webSocketSender.sendToUser(
            wsReq.getRecipient(),  // identifiant utilisateur
            wsReq.getDestination(), // ex: "/queue/notifications"
            wsReq.getPayload()
        );
    }
    
    @Override
    public NotificationType getSupportedType() { return NotificationType.WEBSOCKET; }
}