package org.metavm.user.rest.dto;

import org.jsonk.Json;

@Json
public record IssueTokenRequest(String userId) {
}
