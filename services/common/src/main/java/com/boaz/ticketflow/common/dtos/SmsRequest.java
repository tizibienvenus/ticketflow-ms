package com.boaz.ticketflow.common.dtos;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@SuperBuilder
@Jacksonized
public class SmsRequest extends NotificationRequest {
    private final String message; // ou code
    // ...
}