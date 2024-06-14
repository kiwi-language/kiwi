package org.metavm.user.rest.dto;

import org.metavm.common.BaseDTO;

public record RoleDTO (
        String id,
        String name
) implements BaseDTO {

    public static RoleDTO create(String id, String name) {
        return new RoleDTO(id, name);
    }

}
