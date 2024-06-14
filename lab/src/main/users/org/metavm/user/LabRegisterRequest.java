package org.metavm.user;

import org.metavm.entity.EntityType;

@EntityType
public record LabRegisterRequest(
        String loginName,
        String name,
        String password,
        String verificationCode
) {
}
