package tech.metavm.user;

import tech.metavm.entity.EntityType;

import javax.annotation.Nullable;

@EntityType(value = "登录结果", ephemeral = true)
public record LabLoginResult(
        @Nullable LabToken token,
        LabUser user
) {
}
