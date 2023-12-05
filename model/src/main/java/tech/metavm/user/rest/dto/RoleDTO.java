package tech.metavm.user.rest.dto;

import tech.metavm.common.BaseDTO;

public record RoleDTO (
        Long id,
        Long tmpId,
        String name
) implements BaseDTO {

    public static RoleDTO create(Long id, String name) {
        return new RoleDTO(id, null, name);
    }

}
