package org.manul.user.rest.dto;

import org.jsonk.Json;

@Json
public record AuthenticateRequest(String token) {
}
