package tech.metavm.event.rest.dto;

import tech.metavm.event.UserEventKind;
import tech.metavm.message.rest.dto.MessageDTO;

import java.util.Objects;

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
