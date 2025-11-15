package org.metavm.event.rest.dto;

import lombok.Getter;
import org.jsonk.Json;
import org.jsonk.SubType;

import java.util.Objects;

@Getter
@Json(
        typeProperty = "kind",
        subTypes = {
                @SubType(value = "1", type = TypeChangeEvent.class)
        }
)
public abstract class AppEvent {
    private final long appId;
    private final int kind;

    public AppEvent(long appId, int kind) {
        this.appId = appId;
        this.kind = kind;
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
