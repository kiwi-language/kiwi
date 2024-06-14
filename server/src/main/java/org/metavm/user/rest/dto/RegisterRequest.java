package org.metavm.user.rest.dto;

public record RegisterRequest(
        String loginName,
        String name,
        String password,
        String verificationCode
) {
}
