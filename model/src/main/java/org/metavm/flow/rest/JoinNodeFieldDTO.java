package org.metavm.flow.rest;

import org.metavm.object.type.rest.dto.FieldDTO;
import org.metavm.object.type.rest.dto.FieldDTOBuilder;

import java.util.List;

public record JoinNodeFieldDTO(
        String name,
        String fieldId,
        String typeId,
        List<JoinedValueDTO> values
) implements FieldReferringDTO<JoinNodeFieldDTO> {

    public JoinNodeFieldDTO copyWithFieldId(String fieldId) {
        return new JoinNodeFieldDTO(name, fieldId, typeId, values);
    }

    public FieldDTO toFieldDTO() {
        return FieldDTOBuilder.newBuilder(name, typeId)
                .id(fieldId)
                .readonly(true)
                .build();
    }

}
