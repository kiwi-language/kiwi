package tech.metavm.flow.rest;

import tech.metavm.object.type.rest.dto.FieldDTO;
import tech.metavm.object.type.rest.dto.FieldDTOBuilder;

public record LoopFieldDTO(
        String fieldId,
        String name,
        String type,
        ValueDTO initialValue,
        ValueDTO updatedValue
) implements FieldReferringDTO<LoopFieldDTO> {

    public LoopFieldDTO copyWithFieldId(String fieldId) {
        return new LoopFieldDTO(fieldId, name, type, initialValue, updatedValue);
    }

    public FieldDTO toFieldDTO() {
        return FieldDTOBuilder.newBuilder(name,  type)
                .id(fieldId)
                .build();
    }

}
