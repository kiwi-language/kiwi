package org.metavm.application.rest.dto;

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
