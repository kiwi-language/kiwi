package tech.metavm.event.rest.dto;

import tech.metavm.event.UserEventKind;

import java.util.Objects;

public class LeaveAppEvent extends UserEvent{

    private final long appId;

    public LeaveAppEvent(long userId, long appId) {
        super(UserEventKind.LEAVE_APP.code(), userId);
        this.appId = appId;
    }

    public long getAppId() {
        return appId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof LeaveAppEvent that)) return false;
        if (!super.equals(object)) return false;
        return appId == that.appId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), appId);
    }
}
