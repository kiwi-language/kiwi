package tech.metavm.flow.rest;

import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.type.rest.dto.FieldDTO;
import tech.metavm.object.type.rest.dto.FieldDTOBuilder;

import javax.annotation.Nullable;

public record InputFieldDTO(
        String fieldId,
        String name,
        String type,
        FieldValue defaultValue,
        @Nullable ValueDTO condition
) implements FieldReferringDTO<InputFieldDTO> {

    public static InputFieldDTO create(String name, String type) {
        return new InputFieldDTO(null, name, type, null, null);
    }

    public InputFieldDTO copyWithFieldId(String fieldId) {
        return new InputFieldDTO(fieldId, name, type, defaultValue, condition);
    }

    public FieldDTO toFieldDTO(String declaringTypeId) {
        return FieldDTOBuilder.newBuilder(name, type)
                .id(fieldId)
                .readonly(true)
                .defaultValue(defaultValue)
                .declaringTypeId(declaringTypeId)
                .build();
    }

}
