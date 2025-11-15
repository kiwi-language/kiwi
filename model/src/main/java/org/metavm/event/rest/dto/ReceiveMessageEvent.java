package org.metavm.event.rest.dto;

import lombok.Getter;
import org.jsonk.Json;
import org.metavm.event.UserEventKind;
import org.metavm.message.rest.dto.MessageDTO;

@Getter
@Json

public class ReceiveMessageEvent extends UserEvent {

    private final MessageDTO message;

    public ReceiveMessageEvent(MessageDTO message) {
        super(UserEventKind.RECEIVE_MESSAGE.code(), message.receiverId());
        this.message = message;
    }

}
