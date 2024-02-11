package tech.metavm.user;

import tech.metavm.entity.EntityType;

import javax.annotation.Nullable;

@EntityType(value = "登录结果", ephemeral = true)
public final class LabLoginResult {
    @Nullable
    private final String token;
    private final LabUser user;

    public LabLoginResult(
            @Nullable String token,
            LabUser user
    ) {
        this.token = token;
        this.user = user;
    }

    @Nullable
    public String token() {
        return token;
    }

    public LabUser user() {
        return user;
    }

}
