package org.metavm.event.rest.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Objects;

@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = ReceiveMessageEvent.class, name = "1"),
                @JsonSubTypes.Type(value = ReadMessageEvent.class, name = "2"),
                @JsonSubTypes.Type(value = JoinAppEvent.class, name = "10"),
                @JsonSubTypes.Type(value = LeaveAppEvent.class, name = "11"),
        }
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind", visible = true, include = JsonTypeInfo.As.EXISTING_PROPERTY)
public abstract class UserEvent {

    private final int kind;
    private final String userId;

    public UserEvent(int kind, String userId) {
        this.kind = kind;
        this.userId = userId;
    }

    public int getKind() {
        return kind;
    }

    public String getUserId() {
        return userId;
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
