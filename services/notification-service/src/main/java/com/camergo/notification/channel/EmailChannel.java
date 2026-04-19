package com.camergo.notification.channel;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.boaz.ticketflow.common.domain.NotificationType;
import com.boaz.ticketflow.common.dtos.Attachment;
import com.boaz.ticketflow.common.dtos.EmailRequest;
import com.boaz.ticketflow.common.dtos.NotificationRequest;
import com.boaz.ticketflow.common.exceptions.NotificationException;

import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailChannel implements NotificationChannel {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Override
    public NotificationType getSupportedType() { return NotificationType.EMAIL; }

    @Override
    public void send(NotificationRequest request) {
        EmailRequest emailReq = (EmailRequest) request;
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(emailReq.getRecipient());
            helper.setSubject(emailReq.getSubject());

            // Génération du contenu HTML à partir du template
            Context context = new Context();
            context.setVariables(emailReq.getTemplateData());
            String htmlContent = templateEngine.process(emailReq.getTemplateName(), context);
            helper.setText(htmlContent, true);

            // Pièces jointes
            for (Attachment att : emailReq.getAttachments()) {
                helper.addAttachment(att.getFilename(), new ByteArrayResource(att.getContent()));
            }

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new NotificationException("Failed to send email", e);
        }
    }
}