package tech.metavm.application.rest.dto;

public record PromoteRequest(
        long appId,
        long userId
) {
}
