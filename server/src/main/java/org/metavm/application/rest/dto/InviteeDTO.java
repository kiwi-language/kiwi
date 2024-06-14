package org.metavm.application.rest.dto;

public record InviteeDTO(
        String id,
        String loginName,
        boolean inApp
) {
}
