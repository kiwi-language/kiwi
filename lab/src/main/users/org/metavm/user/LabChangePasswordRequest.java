package org.metavm.user;

import org.metavm.api.EntityStruct;

@EntityStruct
public record LabChangePasswordRequest(
        String verificationCode,
        String loginName,
        String password
) {
}
