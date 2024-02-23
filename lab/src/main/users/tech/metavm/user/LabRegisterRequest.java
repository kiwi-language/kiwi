package tech.metavm.user;

public record LabRegisterRequest(
        String loginName,
        String name,
        String password,
        String verificationCode
) {
}
