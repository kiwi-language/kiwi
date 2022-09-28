package tech.metavm.flow.rest;

import tech.metavm.object.meta.Access;
import tech.metavm.object.meta.rest.dto.FieldDTO;

import java.util.List;

public record InputFieldDTO (
        Long id,
        String name,
        int type,
        Long targetId,
        Object defaultValue,
        boolean required
){

    public FieldDTO toFieldDTO(long typeId) {
        return new FieldDTO(
                id,
                name,
                type,
                Access.Public.code(),
                required,
                defaultValue,
                false,
                false,
                    false,
                typeId,
                targetId,
                null,
                List.of(),
                null
        );
    }

}
