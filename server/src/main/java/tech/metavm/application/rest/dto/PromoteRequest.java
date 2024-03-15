package tech.metavm.application.rest.dto;

public record PromoteRequest(
        String appId,
        String userId
) {
}
