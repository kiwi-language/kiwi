package tech.metavm.application.rest.dto;

public record AppInvitationDTO(
        long userId,
        long appId,
        String title,
        boolean isAdmin,
        int state
) {
}
