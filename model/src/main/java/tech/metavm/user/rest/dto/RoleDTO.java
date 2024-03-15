package tech.metavm.user.rest.dto;

import tech.metavm.common.BaseDTO;

public record RoleDTO (
        String id,
        String name
) implements BaseDTO {

    public static RoleDTO create(String id, String name) {
        return new RoleDTO(id, name);
    }

}
