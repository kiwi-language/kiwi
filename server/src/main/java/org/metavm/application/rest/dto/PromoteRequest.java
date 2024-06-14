package org.metavm.application.rest.dto;

public record PromoteRequest(
        long appId,
        String userId
) {
}
