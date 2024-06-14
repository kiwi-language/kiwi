package org.metavm.application.rest.dto;

public record DemoteRequest(
        long appId,
        String userId
) {
}
