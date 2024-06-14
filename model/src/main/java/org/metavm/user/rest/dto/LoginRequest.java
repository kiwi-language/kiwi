package org.metavm.user.rest.dto;

public record LoginRequest(
        long appId,
        String loginName,
        String password
) {
}
