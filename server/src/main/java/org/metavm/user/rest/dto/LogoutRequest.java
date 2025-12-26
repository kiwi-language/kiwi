package org.metavm.user.rest.dto;

import org.jsonk.Json;

@Json
public record LogoutRequest(String token) {
}
