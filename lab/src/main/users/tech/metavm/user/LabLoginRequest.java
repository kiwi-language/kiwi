package tech.metavm.user;

import tech.metavm.entity.EntityType;

@EntityType(value = "登录请求", ephemeral = true)
public final class LabLoginRequest {
    private final long appId;
    private final String loginName;
    private final String password;

    public LabLoginRequest(
            long appId,
            String loginName,
            String password
    ) {
        this.appId = appId;
        this.loginName = loginName;
        this.password = password;
    }

    public long appId() {
        return appId;
    }

    public String loginName() {
        return loginName;
    }

    public String password() {
        return password;
    }

}
