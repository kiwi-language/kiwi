package org.manul.application.rest.dto;

public record DemoteRequest(
        long appId,
        String userId
) {
}
