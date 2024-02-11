package tech.metavm.user;

import tech.metavm.entity.EntityType;

@EntityType(value = "令牌", ephemeral = true)
public final class LabToken {
    private final long appId;
    private final String token;

    public LabToken(
            long appId,
            String token
    ) {
        this.appId = appId;
        this.token = token;
    }

    public long appId() {
        return appId;
    }

    public String token() {
        return token;
    }

}
