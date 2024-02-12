package tech.metavm.user;


import tech.metavm.entity.EntityType;

import javax.annotation.Nullable;

@EntityType(value = "登录信息", ephemeral = true)
public record LabLoginInfo(long appId, @Nullable LabUser user) {

    public static LabLoginInfo failed() {
        return new LabLoginInfo(-1L, null);
    }

}
