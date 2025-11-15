package org.metavm.user.rest.dto;

import org.jsonk.Json;

@Json
public record LoginRequest(
        long appId,
        String loginName,
        String password
) {
}
