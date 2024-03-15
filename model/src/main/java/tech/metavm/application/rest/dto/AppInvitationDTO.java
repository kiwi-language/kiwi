package tech.metavm.application.rest.dto;

public record AppInvitationDTO(
        String userId,
        String appId,
        String title,
        boolean isAdmin,
        int state
) {
}
