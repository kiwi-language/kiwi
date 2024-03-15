package tech.metavm.application.rest.dto;

public record AppInvitationRequest(
        String appId,
        String userId,
        boolean isAdmin
) {
}
