package tech.metavm.flow.rest;

import tech.metavm.object.type.rest.dto.FieldDTO;
import tech.metavm.object.type.rest.dto.FieldDTOBuilder;

import java.util.ArrayList;
import java.util.List;

public record TryEndFieldDTO(
        String name,
        String fieldId,
        String typeId,
        List<TryEndValueDTO> values,
        ValueDTO defaultValue
) implements FieldReferringDTO<TryEndFieldDTO> {

    @Override
    public TryEndFieldDTO copyWithFieldId(String fieldId) {
        return new TryEndFieldDTO(
                name,
                fieldId,
                typeId,
                new ArrayList<>(values),
                defaultValue
        );
    }

    public FieldDTO toFieldDTO() {
        return FieldDTOBuilder.newBuilder(name, typeId)
                .id(fieldId)
                .readonly(true)
                .build();
    }

}
