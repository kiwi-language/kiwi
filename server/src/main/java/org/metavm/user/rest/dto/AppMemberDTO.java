package org.metavm.user.rest.dto;

public record AppMemberDTO(
        String id,
        String name,
        boolean isAdmin,
        boolean isOwner
) {
}
