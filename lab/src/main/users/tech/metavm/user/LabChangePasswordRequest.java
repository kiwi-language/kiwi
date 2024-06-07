package tech.metavm.user;

import tech.metavm.entity.EntityType;

@EntityType(ephemeral = true)
public record LabChangePasswordRequest(
        String verificationCode,
        String loginName,
        String password
) {
}
