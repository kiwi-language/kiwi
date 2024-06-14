package org.metavm.event.rest.dto;

import org.metavm.event.UserEventKind;

public class ReadMessageEvent extends UserEvent{

    private final String messageId;

    public ReadMessageEvent(String userId, String messageId) {
        super(UserEventKind.READ_MESSAGE.code(), userId);
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }
}
