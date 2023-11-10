package tech.metavm.flow.rest;

import tech.metavm.dto.RefDTO;
import tech.metavm.object.meta.Access;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.util.NncUtils;

public record OutputFieldDTO(
        RefDTO fieldRef,
        String name,
        RefDTO typeRef,
        ValueDTO value
) implements FieldReferringDTO<OutputFieldDTO> {

    public Long id() {
        return fieldRef.id();
    }

    public Long typeId() {
        return typeRef.id();
    }

    public OutputFieldDTO copyWithId(long id) {
        return new OutputFieldDTO(
                RefDTO.ofId(id),
                name,
                typeRef,
                value
        );
    }

    public OutputFieldDTO copyWithFieldRef(RefDTO fieldRef) {
        return new OutputFieldDTO(
                fieldRef, name, typeRef, value
        );
    }

    public FieldDTO toFieldDTO() {
        return new FieldDTO(
                NncUtils.get(fieldRef, RefDTO::tmpId),
                NncUtils.get(fieldRef, RefDTO::id),
                name,
                null,
                Access.PUBLIC.code(),
                null,
                false,
                false,
                null,
                typeRef,
                false,
                false
        );
    }

}
