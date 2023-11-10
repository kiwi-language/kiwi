package tech.metavm.flow.rest;

import tech.metavm.dto.RefDTO;
import tech.metavm.object.meta.Access;
import tech.metavm.object.meta.rest.dto.FieldDTO;

import java.util.ArrayList;
import java.util.List;

public record TryEndFieldDTO(
        String name,
        RefDTO fieldRef,
        RefDTO typeRef,
        List<TryEndValueDTO> values,
        ValueDTO defaultValue
) implements FieldReferringDTO<TryEndFieldDTO> {

    @Override
    public TryEndFieldDTO copyWithFieldRef(RefDTO fieldRef) {
        return new TryEndFieldDTO(
                name,
                fieldRef,
                typeRef,
                new ArrayList<>(values),
                defaultValue
        );
    }

    public FieldDTO toFieldDTO() {
        return new FieldDTO(
                fieldRef.tmpId(),
                fieldRef.id(),
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
