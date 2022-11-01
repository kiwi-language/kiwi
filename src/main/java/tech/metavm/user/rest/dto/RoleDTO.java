package tech.metavm.user.rest.dto;

public record RoleDTO (
        Long id,
        String name
) {

    public static RoleDTO create(Long id, String name) {
        return new RoleDTO(id, name);
    }

}
