package tech.metavm.flow.rest;

import tech.metavm.common.RefDTO;
import tech.metavm.object.type.rest.dto.FieldDTO;
import tech.metavm.object.type.rest.dto.FieldDTOBuilder;

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
        return FieldDTOBuilder.newBuilder(name, typeRef)
                .tmpId(fieldRef().tmpId())
                .id(fieldRef.id())
                .build();
    }

}
