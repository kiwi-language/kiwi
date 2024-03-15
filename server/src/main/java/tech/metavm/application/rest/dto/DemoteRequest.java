package tech.metavm.application.rest.dto;

public record DemoteRequest(
        String appId,
        String userId
) {
}
