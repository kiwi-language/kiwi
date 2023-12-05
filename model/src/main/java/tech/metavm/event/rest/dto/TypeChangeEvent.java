package tech.metavm.event.rest.dto;

import tech.metavm.event.AppEventKind;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class TypeChangeEvent extends AppEvent {
    private final long version;
    private final List<Long> typeIds;
    @Nullable
    private final String triggerClientId;

    public TypeChangeEvent(long appId, long version, List<Long> typeIds, @Nullable String triggerClientId) {
        super(appId, AppEventKind.TYPE_CHANGE.code());
        this.version = version;
        this.typeIds = typeIds;
        this.triggerClientId = triggerClientId;
    }

    public long getVersion() {
        return version;
    }

    public List<Long> getTypeIds() {
        return typeIds;
    }

    @Nullable
    public String getTriggerClientId() {
        return triggerClientId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof TypeChangeEvent that)) return false;
        if (!super.equals(object)) return false;
        return version == that.version && Objects.equals(typeIds, that.typeIds) && Objects.equals(triggerClientId, that.triggerClientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), version, typeIds, triggerClientId);
    }
}
