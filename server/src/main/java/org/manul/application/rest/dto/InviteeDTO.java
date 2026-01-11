package org.manul.application.rest.dto;

public record InviteeDTO(
        String id,
        String loginName,
        boolean inApp
) {
}
