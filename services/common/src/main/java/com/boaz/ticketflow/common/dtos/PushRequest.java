package com.boaz.ticketflow.common.dtos;

import java.util.Map;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class PushRequest extends NotificationRequest {
    private final String title;
    private final String body;
    private final String imageUrl;
    private final Map<String, String> data;
}
