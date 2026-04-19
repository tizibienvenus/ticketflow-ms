package com.camergo.notification.channel;


import org.springframework.stereotype.Component;

import com.boaz.ticketflow.common.domain.NotificationType;
import com.boaz.ticketflow.common.dtos.NotificationRequest;
import com.boaz.ticketflow.common.dtos.SmsRequest;
import com.camergo.notification.config.TwilioProperties;


import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;


import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SmsChannel implements NotificationChannel {

    private final TwilioProperties properties;

    @Override
    public NotificationType getSupportedType() { return NotificationType.SMS; }

    @Override
    public void send(NotificationRequest request) {
        SmsRequest emailReq = (SmsRequest) request;
        try {
            if (isPhoneNumberValid(emailReq.getRecipient())) {
                PhoneNumber toPhoneNumber = new PhoneNumber(emailReq.getRecipient());
                PhoneNumber fromPhoneNumber = new PhoneNumber(properties.getPhoneNumber());
                Message.creator(toPhoneNumber, fromPhoneNumber, emailReq.getMessage()).create();
            } else {
                throw new IllegalArgumentException(
                    "Phone number [" + emailReq.getRecipient() + "] is not a valid number"
                );
            }
        } catch (ApiException exception) {
            throw new RuntimeException("Error sending OTP SMS: " + exception.getMessage(), exception);
        }
    }

    private boolean isPhoneNumberValid(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("\\+?[1-9]\\d{1,14}$");
    }
}
