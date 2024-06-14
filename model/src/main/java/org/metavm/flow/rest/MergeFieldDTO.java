package org.metavm.flow.rest;

import org.metavm.object.type.rest.dto.FieldDTO;
import org.metavm.object.type.rest.dto.FieldDTOBuilder;

import java.util.List;

public record MergeFieldDTO(
        String name,
        String fieldId,
        String typeId,
        List<ConditionalValueDTO> values
) implements FieldReferringDTO<MergeFieldDTO> {

    public MergeFieldDTO copyWithFieldId(String fieldId) {
        return new MergeFieldDTO(name, fieldId, typeId, values);
    }

    public FieldDTO toFieldDTO() {
        return FieldDTOBuilder.newBuilder(name, typeId)
                .id(fieldId)
                .readonly(true)
                .build();
    }

}
