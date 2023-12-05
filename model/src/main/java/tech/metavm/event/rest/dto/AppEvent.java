package tech.metavm.event.rest.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Objects;

@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = TypeChangeEvent.class, name = "1")
        }
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind", visible = true, include = JsonTypeInfo.As.EXISTING_PROPERTY)
public abstract class AppEvent {
    private final long appId;
    private final int kind;

    public AppEvent(long appId, int kind) {
        this.appId = appId;
        this.kind = kind;
    }

    public long getAppId() {
        return appId;
    }

    public int getKind() {
        return kind;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof AppEvent appEvent)) return false;
        return appId == appEvent.appId && kind == appEvent.kind;
    }

    @Override
    public int hashCode() {
        return Objects.hash(appId, kind);
    }
}
