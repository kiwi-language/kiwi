package org.manul.user.rest.dto;

import org.manul.common.rest.dto.BaseDTO;

public record RoleDTO (
        String id,
        String name
) implements BaseDTO {

    public static RoleDTO create(String id, String name) {
        return new RoleDTO(id, name);
    }

}
