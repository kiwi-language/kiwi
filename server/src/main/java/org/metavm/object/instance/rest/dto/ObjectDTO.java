package org.metavm.object.instance.rest.dto;

import org.metavm.api.dto.ClassTypeDTO;

import java.util.List;

public record ObjectDTO(
        String id,
        ClassTypeDTO type,
        List<FieldDTO> fields,
        List<ObjectDTO> children
) implements ValueDTO {
    @Override
    public String getKind() {
        return "object";
    }
}
