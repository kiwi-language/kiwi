package org.manul.util;

import org.manul.entity.natives.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

public class MockEmailSender implements EmailSender {

    public static final MockEmailSender INSTANCE = new MockEmailSender();

    private MockEmailSender() {
    }

    public static final Logger logger = LoggerFactory.getLogger(MockEmailSender.class);

    private @Nullable Email lastSentEmail;

    @Override
    public void send(String recipient, String subject, String content) {
        logger.info("MockEmailSender: send email to " + recipient + " with subject " + subject + " and content " + content);
        lastSentEmail = new Email(recipient, subject, content);
    }

    public @Nullable Email getLastSentEmail() {
        return lastSentEmail;
    }

    public record Email(String recipient, String subject, String content) {
    }

}
