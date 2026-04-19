package com.boaz.ticketflow.common.dtos;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class WebSocketRequest extends NotificationRequest {
    private final String destination;
    private final Object payload;
    // ...
}