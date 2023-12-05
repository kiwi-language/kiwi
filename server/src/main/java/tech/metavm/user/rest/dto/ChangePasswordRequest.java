package tech.metavm.user.rest.dto;

public record ChangePasswordRequest(
        String verificationCode,
        String loginName,
        String password
) {
}
