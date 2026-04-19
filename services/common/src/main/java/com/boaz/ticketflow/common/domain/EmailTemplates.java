package com.boaz.ticketflow.common.domain;

import lombok.Getter;

public enum EmailTemplates {

    INVOICE_RIDE("invoice-ride.html", "Facture de votre course"),
    LOYALTY_POINTS("loyalty-points.html", "Vos points de fidélité"),
    ORDER_DELIVERED("order-delivered.html", "Commande livrée"),
    OTP_EMAIL("otp-email.html", "Votre code de validation"),
    PROMO_OFFER("promo-offer.html", "Offre promotionnelle"),
    SYSTEM_UPDATE("system-update.html", "Mise à jour du système"),
    WELCOME_PLATFORM("welcome-platform.html", "Bienvenue sur la plateforme"),
    KYC_NOT_VALIDATED("kyc-not-validated.html", "KYC non validé"),
    ORDER_CANCELLED("order-cancelled.html", "Commande annulée"),
    ORDER_ON_THE_WAY("order-on-the-way.html", "Commande en route"),
    PAYMENT_FAILED("payment-failed.html", "Paiement échoué"),
    REFERRAL_NOTIFICATION("referral-notification.html", "Notification de parrainage"),
    TICKET_RECEIVED("ticket-received.html", "Ticket reçu"),
    KYC_VALIDATED("kyc-validated.html", "KYC validé"),
    ORDER_CONFIRMATION("order-confirmation.html", "Confirmation de commande"),
    ORDER_PREPARATION("order-preparation.html", "Commande en préparation"),
    PAYMENT_SUCCESS("payment-success.html", "Paiement réussi"),
    SECURITY_ALERT("security-alert.html", "Alerte de sécurité"),
    WALLET_TOPUP("wallet-topup.html", "Recharge de portefeuille");

    @Getter
    private final String template;
    @Getter
    private final String subject;

    EmailTemplates(String template, String subject) {
        this.template = template;
        this.subject = subject;
    }
}