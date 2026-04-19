package com.boaz.ticketflow.common.dtos;

import lombok.Data;

import java.time.LocalDateTime;

import lombok.Builder;

@Data
@Builder
public class InvoiceEvent {
    private String userEmail;
    private String userName;

    // Référence facture
    private String invoiceReference;

    // ID course
    private String rideId;

    // Date de la course
    private LocalDateTime rideDate;

    // Type de service
    private String service;

    // Description du trajet
    private String description;

    // Montants
    private double amount;
    private double tva;
    private double total;

    // Moyen de paiement
    private String paymentMethod;
}