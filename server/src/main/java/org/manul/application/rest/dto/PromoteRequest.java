package org.manul.application.rest.dto;

public record PromoteRequest(
        long appId,
        String userId
) {
}
