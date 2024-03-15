package tech.metavm.event.rest.dto;

import tech.metavm.event.UserEventKind;

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
