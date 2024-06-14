package org.metavm.entity.natives;

public interface EmailSender {

    void send(String recipient, String subject, String content);

}
