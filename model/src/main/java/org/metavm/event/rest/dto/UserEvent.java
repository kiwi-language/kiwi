package org.metavm.event.rest.dto;

import lombok.Getter;
import org.jsonk.Json;

import java.util.Objects;

@Getter
@Json(
        typeProperty = "kind",
        subTypes = {
                @org.jsonk.SubType(value = "1", type = ReceiveMessageEvent.class),
                @org.jsonk.SubType(value = "2", type = ReadMessageEvent.class),
                @org.jsonk.SubType(value = "10", type = JoinAppEvent.class),
                @org.jsonk.SubType(value = "11", type = LeaveAppEvent.class),
        }
)
public abstract class UserEvent {

    private final int kind;
    private final String userId;

    public UserEvent(int kind, String userId) {
        this.kind = kind;
        this.userId = userId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof UserEvent userEvent)) return false;
        return kind == userEvent.kind && Objects.equals(userId, userEvent.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, userId);
    }
}
