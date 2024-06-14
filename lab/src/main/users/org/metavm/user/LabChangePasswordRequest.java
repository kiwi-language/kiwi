package org.metavm.user;

import org.metavm.entity.EntityStruct;

@EntityStruct
public record LabChangePasswordRequest(
        String verificationCode,
        String loginName,
        String password
) {
}
