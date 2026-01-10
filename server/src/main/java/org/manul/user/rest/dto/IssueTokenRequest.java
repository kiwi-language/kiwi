package org.manul.user.rest.dto;

import org.jsonk.Json;

@Json
public record IssueTokenRequest(String userId) {
}
