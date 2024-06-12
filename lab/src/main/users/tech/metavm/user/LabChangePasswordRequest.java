package tech.metavm.user;

import tech.metavm.entity.EntityStruct;

@EntityStruct
public record LabChangePasswordRequest(
        String verificationCode,
        String loginName,
        String password
) {
}
