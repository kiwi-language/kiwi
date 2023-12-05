package tech.metavm.application.rest.dto;

public record DemoteRequest(
        long appId,
        long userId
) {
}
