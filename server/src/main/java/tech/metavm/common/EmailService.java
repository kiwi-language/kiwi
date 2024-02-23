package tech.metavm.common;

public interface EmailService {
    void send(String recipient, String subject, String content);
}
