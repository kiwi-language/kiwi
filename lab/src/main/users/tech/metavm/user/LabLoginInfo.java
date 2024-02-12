package tech.metavm.user;


import tech.metavm.entity.EntityType;

import javax.annotation.Nullable;

@EntityType(value = "登录信息", ephemeral = true)
public final class LabLoginInfo {
    private final long appId;
    private final @Nullable LabUser user;

    public LabLoginInfo(long appId, @Nullable LabUser user) {
        this.appId = appId;
        this.user = user;
    }

    public static LabLoginInfo failed() {
        return new LabLoginInfo(-1L, null);
    }

    public long appId() {
        return appId;
    }

    public @Nullable LabUser user() {
        return user;
    }

}
