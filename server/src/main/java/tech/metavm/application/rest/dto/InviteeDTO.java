package tech.metavm.application.rest.dto;

public record InviteeDTO(
        long id,
        String loginName,
        boolean inApp
) {
}
