package tech.metavm.user.rest.dto;

public record LoginRequest(
        String appId,
        String loginName,
        String password
) {
}
