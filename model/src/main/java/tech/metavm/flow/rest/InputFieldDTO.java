package tech.metavm.flow.rest;

import tech.metavm.common.RefDTO;
import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.type.rest.dto.FieldDTO;
import tech.metavm.object.type.rest.dto.FieldDTOBuilder;

import javax.annotation.Nullable;

public record InputFieldDTO(
        RefDTO fieldRef,
        String name,
        RefDTO typeRef,
        FieldValue defaultValue,
        @Nullable ValueDTO condition
) implements FieldReferringDTO<InputFieldDTO> {

    public static InputFieldDTO create(String name, RefDTO typeRef) {
        return new InputFieldDTO(null, name, typeRef, null, null);
    }

    public InputFieldDTO copyWithFieldRef(RefDTO fieldRef) {
        return new InputFieldDTO(fieldRef, name, typeRef, defaultValue, condition);
    }

    public FieldDTO toFieldDTO(Long declaringTypeId) {
        return FieldDTOBuilder.newBuilder(name, typeRef)
                .tmpId(fieldRef.tmpId())
                .id(fieldRef.id())
                .readonly(true)
                .defaultValue(defaultValue)
                .declaringTypeId(declaringTypeId)
                .build();
    }

}
