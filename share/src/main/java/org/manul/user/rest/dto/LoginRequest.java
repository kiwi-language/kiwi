package org.manul.user.rest.dto;

import org.jsonk.Json;

@Json
public record LoginRequest(
        long appId,
        String loginName,
        String password
) {
}
