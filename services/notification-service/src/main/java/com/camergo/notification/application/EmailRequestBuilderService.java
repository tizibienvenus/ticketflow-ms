package com.camergo.notification.application;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.boaz.ticketflow.common.domain.EmailTemplates;
import com.boaz.ticketflow.common.dtos.EmailRequest;
import com.boaz.ticketflow.common.dtos.InvoiceEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class EmailRequestBuilderService {

    public EmailRequest buildFromInvoiceRideEvent(InvoiceEvent event) {
        return EmailRequest.builder()
            .recipient(event.getUserEmail())
            .subject(EmailTemplates.INVOICE_RIDE.getSubject())
            .templateName(EmailTemplates.INVOICE_RIDE.getTemplate())
            .templateData(toMap(event))  // convertit l'objet en Map
            .build();
    }

    // Méthode utilitaire pour convertir un objet en Map (via Jackson ou reflection)
    private Map<String, Object> toMap(Object data) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(data, new TypeReference<Map<String, Object>>() {});
    }
}