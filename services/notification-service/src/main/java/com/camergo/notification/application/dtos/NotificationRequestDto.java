package com.camergo.notification.application.dtos;

import java.util.List;
import java.util.Map;

import com.boaz.ticketflow.common.domain.NotificationType;
import com.boaz.ticketflow.common.dtos.AttachmentDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor

public class NotificationRequestDto {
    private NotificationType type;       // EMAIL, PUSH, SMS, WEBSOCKET
    private String recipient;            // email, token FCM, téléphone, userId
    private String subject;              // pour email
    private String template;              // nom du template email
    private Map<String, Object> variables; // variables du template email
    private List<AttachmentDto> attachments; // pièces jointes éventuelles
    private String title;                 // pour push
    private String body;                   // pour push ou sms
    private String imageUrl;               // pour push
    private Map<String, String> data;       // métadonnées pour push
    private String destination;             // pour WebSocket (topic ou queue)
    private Object payload;                 // pour WebSocket
    private Map<String, Object> metadata;   // infos communes supplémentaires

    // getters et setters...
}