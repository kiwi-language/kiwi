package tech.metavm.common;

public interface EmailService {
    void send(String receiver, String title, String content);
}
