package com.boaz.ticketflow.common.dtos;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@SuperBuilder
@Jacksonized
public class EmailRequest extends NotificationRequest {
    private final String subject;
    private final String templateName;

    @lombok.Builder.Default
    private final Map<String, Object> templateData = new HashMap<>();

    @lombok.Builder.Default
    private final List<Attachment> attachments = new ArrayList<>();
}
