package org.metavm.event.rest.dto;

import lombok.Getter;
import org.jsonk.Json;
import org.metavm.event.UserEventKind;

@Getter
@Json
public class ReadMessageEvent extends UserEvent{

    private final String messageId;

    public ReadMessageEvent(String userId, String messageId) {
        super(UserEventKind.READ_MESSAGE.code(), userId);
        this.messageId = messageId;
    }

}
