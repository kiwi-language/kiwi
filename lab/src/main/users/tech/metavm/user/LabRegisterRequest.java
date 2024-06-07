package tech.metavm.user;

import tech.metavm.entity.EntityType;

@EntityType
public record LabRegisterRequest(
        String loginName,
        String name,
        String password,
        String verificationCode
) {
}
