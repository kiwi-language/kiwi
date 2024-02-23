package tech.metavm.user;

import tech.metavm.entity.EntityType;

@EntityType("注册请求")
public record LabRegisterRequest(
        String loginName,
        String name,
        String password,
        String verificationCode
) {
}
