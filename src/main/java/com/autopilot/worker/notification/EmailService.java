package com.autopilot.worker.notification;

import com.autopilot.config.logging.AppLogger;
import com.autopilot.models.payload.Contact;
import com.mailersend.sdk.MailerSend;
import com.mailersend.sdk.MailerSendResponse;
import com.mailersend.sdk.emails.Email;
import com.mailersend.sdk.exceptions.MailerSendException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final AppLogger log = new AppLogger(LoggerFactory.getLogger(EmailService.class));
    private final MailerSend mailerSend;
    private final String templateId;
    private final String fromEmail;
    private final String fromName;
    private final String toEmail;
    private final String toName;

    public EmailService(
            @Value("${mailersend.api.key}") String apiKey,
            @Value("${mailersend.template.id}") String templateId,
            @Value("${mailersend.from.email}") String fromEmail,
            @Value("${mailersend.from.name}") String fromName,
            @Value("${mailersend.to.email}") String toEmail,
            @Value("${mailersend.to.name}") String toName
    ) {
        this.mailerSend = new MailerSend();
        this.mailerSend.setToken(apiKey);
        this.templateId = templateId;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
        this.toEmail = toEmail;
        this.toName = toName;
    }

    /**
     * Send email using configured template.
     *
     * @param personalization Contact object containing sender info
     */
    public void sendEmail(Contact personalization) {
        String senderName = personalization.getName();
        String senderEmail = personalization.getEmail();
        String subject = personalization.getSubject();
        String message = personalization.getMessage();

        log.info("Preparing to send email from '{}' <{}>", senderName, senderEmail);

        try {
            Email email = new Email();

            // Use verified sender
            email.setFrom(fromName, fromEmail);
            email.setSubject("New Contact Message: " + subject);

            // Send to admin/verified recipient
            email.addRecipient(toName, toEmail);

            email.setTemplateId(templateId);

            // Add template variables
            email.addPersonalization("name", senderName);
            email.addPersonalization("email", senderEmail);
            email.addPersonalization("subject", subject);
            email.addPersonalization("message", message);

            log.info("Sending email to '{}' using template '{}'", toEmail, templateId);

            MailerSendResponse response = mailerSend.emails().send(email);

            log.info("Email sent successfully. Message ID: {}", response.messageId);

        } catch (MailerSendException e) {
            log.error("Failed to send email from '{}' <{}>: {}", fromName, fromEmail, e.getMessage(), e);
        }
    }
}
