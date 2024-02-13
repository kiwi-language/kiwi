package tech.metavm.user;


import tech.metavm.application.LabApplication;
import tech.metavm.entity.EntityType;

import javax.annotation.Nullable;

@EntityType(value = "登录信息", ephemeral = true)
public record LabLoginInfo(@Nullable LabApplication application, @Nullable LabUser user) {

    public static LabLoginInfo failed() {
        return new LabLoginInfo(null, null);
    }

}
