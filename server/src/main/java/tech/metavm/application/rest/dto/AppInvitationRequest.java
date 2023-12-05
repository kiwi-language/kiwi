package tech.metavm.application.rest.dto;

public record AppInvitationRequest(
        long appId,
        long userId,
        boolean isAdmin
) {
}
