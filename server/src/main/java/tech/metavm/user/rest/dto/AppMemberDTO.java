package tech.metavm.user.rest.dto;

public record AppMemberDTO(
        long id,
        String name,
        boolean isAdmin,
        boolean isOwner
) {
}
