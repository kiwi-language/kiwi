package org.metavm.flow.rest;

import org.metavm.object.type.rest.dto.FieldDTO;
import org.metavm.object.type.rest.dto.FieldDTOBuilder;

import java.util.ArrayList;
import java.util.List;

public record TryExitFieldDTO(
        String name,
        String fieldId,
        String type,
        List<TryExitValueDTO> values,
        ValueDTO defaultValue
) implements FieldReferringDTO<TryExitFieldDTO> {

    @Override
    public TryExitFieldDTO copyWithFieldId(String fieldId) {
        return new TryExitFieldDTO(
                name,
                fieldId,
                type,
                new ArrayList<>(values),
                defaultValue
        );
    }

    public FieldDTO toFieldDTO() {
        return FieldDTOBuilder.newBuilder(name, type)
                .id(fieldId)
                .readonly(true)
                .build();
    }

}
