package org.metavm.application.rest.dto;

public record InviteeQuery(
        long appId,
        String loginName
) {
}
