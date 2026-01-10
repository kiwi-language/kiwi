package org.manul.application.rest.dto;

import org.jsonk.Json;

@Json
public record AppInvitationDTO(
        String userId,
        String appId,
        String title,
        boolean isAdmin,
        int state
) {
}
