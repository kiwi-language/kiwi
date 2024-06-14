package org.metavm.event.rest.dto;

import org.metavm.event.UserEventKind;

import java.util.Objects;

public class JoinAppEvent extends UserEvent {

    private final String appId;

    public JoinAppEvent(String userId, String appId) {
        super(UserEventKind.JOIN_APP.code(), userId);
        this.appId = appId;
    }

    public String getAppId() {
        return appId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof JoinAppEvent that)) return false;
        if (!super.equals(object)) return false;
        return Objects.equals(appId, that.appId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), appId);
    }
}
