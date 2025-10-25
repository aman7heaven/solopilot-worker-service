package com.autopilot.worker.consumer;

import com.autopilot.messaging.IBaseQueueConsumer;
import com.autopilot.models.payload.Contact;
import com.autopilot.models.payload.QueuePayload;
import com.autopilot.worker.notification.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserContactMessageConsumer implements IBaseQueueConsumer<Contact> {

    private final EmailService emailService;

    @Override
    public void onMessageReceived(QueuePayload<Contact> payload) {
        emailService.sendEmail(payload.getPayload());
    }

    @Override
    public void onProcessingFailed(QueuePayload<Contact> payload, Exception exception) {
        if (payload != null) {
            log.error("Failed Contact email for '{}' <{}>: {}", payload.getPayload().getName(),
                    payload.getPayload().getEmail(), exception.getMessage(), exception);
        } else {
            log.error("Failed queue message: {}", exception.getMessage(), exception);
        }
    }

    @Override
    public Class<Contact> getPayloadClass() {
        return Contact.class;
    }
}

