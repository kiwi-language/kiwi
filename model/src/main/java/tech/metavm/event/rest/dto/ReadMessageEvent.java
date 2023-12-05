package tech.metavm.event.rest.dto;

import tech.metavm.event.UserEventKind;

public class ReadMessageEvent extends UserEvent{

    private final long messageId;

    public ReadMessageEvent(long userId, long messageId) {
        super(UserEventKind.READ_MESSAGE.code(), userId);
        this.messageId = messageId;
    }

    public long getMessageId() {
        return messageId;
    }
}
