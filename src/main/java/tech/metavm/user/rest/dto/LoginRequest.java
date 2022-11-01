package tech.metavm.user.rest.dto;

public record LoginRequest(
        long tenantId,
        String loginName,
        String password
) {
}
