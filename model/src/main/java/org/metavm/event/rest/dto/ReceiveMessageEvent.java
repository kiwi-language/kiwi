package org.metavm.event.rest.dto;

import org.metavm.event.UserEventKind;
import org.metavm.message.rest.dto.MessageDTO;

public class ReceiveMessageEvent extends UserEvent {

    private final MessageDTO message;

    public ReceiveMessageEvent(MessageDTO message) {
        super(UserEventKind.RECEIVE_MESSAGE.code(), message.receiverId());
        this.message = message;
    }

    public MessageDTO getMessage() {
        return message;
    }

}
