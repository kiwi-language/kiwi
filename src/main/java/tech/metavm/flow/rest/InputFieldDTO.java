package tech.metavm.flow.rest;

import tech.metavm.object.meta.Access;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.rest.dto.FieldDTO;

import java.util.List;

public record InputFieldDTO (
        Long id,
        String name,
//        int type,
//        Long targetId,
        long typeId,
        Object defaultValue
){

//    public FieldDTO toFieldDTO(long ownerId, Type type) {
//        return new FieldDTO(
//                id,
//                name,
//                type.getCategory().code(),
//                Access.Public.code(),
//                required,
//                defaultValue,
//                false,
//                false,
//                    false,
//                ownerId,
//                type.getConcreteType().getId(),
//                null,
//                List.of(),
//                null
//        );
//    }

}
