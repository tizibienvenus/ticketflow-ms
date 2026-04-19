package com.camergo.notification.factory;

import java.util.Base64;

import org.springframework.stereotype.Component;

import com.boaz.ticketflow.common.dtos.Attachment;
import com.boaz.ticketflow.common.dtos.AttachmentDto;
import com.boaz.ticketflow.common.dtos.EmailRequest;
import com.boaz.ticketflow.common.dtos.NotificationRequest;
import com.boaz.ticketflow.common.dtos.PushRequest;
import com.boaz.ticketflow.common.dtos.SmsRequest;
import com.boaz.ticketflow.common.dtos.WebSocketRequest;
import com.camergo.notification.application.dtos.NotificationRequestDto;

@Component
public class NotificationRequestFactory {
    public NotificationRequest create(NotificationRequestDto dto) {
        switch (dto.getType()) {
            case EMAIL -> {
                EmailRequest.EmailRequestBuilder<?, ?>  emailBuilder = EmailRequest.builder()
                    .recipient(dto.getRecipient())
                    .type(dto.getType())
                    .subject(dto.getSubject())
                    .templateName(dto.getTemplate())
                    .templateData(dto.getVariables());
                
                if (dto.getAttachments() != null) {
                    emailBuilder.attachments(
                        dto.getAttachments().stream()
                            .map(this::convertAttachment)
                            .toList()
                    );
                }
                return emailBuilder.build();
            }
            case PUSH -> {
                PushRequest.PushRequestBuilder<?, ?>  pushBuilder = PushRequest.builder()
                    .recipient(dto.getRecipient())
                    .type(dto.getType())
                    .title(dto.getTitle())
                    .body(dto.getBody())
                    .imageUrl(dto.getImageUrl());
                if (dto.getData() != null) {
                    pushBuilder.data(dto.getData());
                }
                return pushBuilder.build();
            }

            case SMS -> {
                return SmsRequest.builder()
                    .recipient(dto.getRecipient())
                    .type(dto.getType())
                    .message(dto.getBody()) // on utilise body pour le contenu SMS
                    .build();
            }

            case WEBSOCKET -> {
                return WebSocketRequest.builder()
                    .recipient(dto.getRecipient())
                    .type(dto.getType())
                    .destination(dto.getDestination())
                    .payload(dto.getPayload())
                    .build();
            }

            default -> throw new IllegalArgumentException("Type de notification inconnu : " + dto.getType());
        }
    }

    private Attachment convertAttachment(AttachmentDto dto) {
        byte[] content = Base64.getDecoder().decode(dto.getBase64Content());
        return new Attachment(dto.getFilename(), dto.getContentType(), content);
    }
}