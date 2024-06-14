package org.metavm.application.rest.dto;

public record AppInvitationRequest(
        long appId,
        String userId,
        boolean isAdmin
) {
}
