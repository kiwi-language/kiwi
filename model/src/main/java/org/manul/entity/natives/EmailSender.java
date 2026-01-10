package org.manul.entity.natives;

public interface EmailSender {

    void send(String recipient, String subject, String content);

}
