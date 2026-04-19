package com.boaz.ticketflow.common.dtos;


import com.boaz.ticketflow.common.domain.NotificationType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = EmailRequest.class, name = "EMAIL"),
    @JsonSubTypes.Type(value = PushRequest.class, name = "PUSH"),
    @JsonSubTypes.Type(value = SmsRequest.class, name = "SMS"),
    @JsonSubTypes.Type(value = WebSocketRequest.class, name = "WEBSOCKET")
})
public abstract class NotificationRequest {
    private final NotificationType type;
    private final String recipient;
}