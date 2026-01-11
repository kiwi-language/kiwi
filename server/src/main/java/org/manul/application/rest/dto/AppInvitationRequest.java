package org.manul.application.rest.dto;

public record AppInvitationRequest(
        long appId,
        String userId,
        boolean isAdmin
) {
}
