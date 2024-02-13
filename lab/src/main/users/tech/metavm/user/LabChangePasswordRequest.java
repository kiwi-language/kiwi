package tech.metavm.user;

import tech.metavm.entity.EntityType;

@EntityType(value = "修改密码请求", ephemeral = true)
public record LabChangePasswordRequest(
        String verificationCode,
        String loginName,
        String password
) {
}
