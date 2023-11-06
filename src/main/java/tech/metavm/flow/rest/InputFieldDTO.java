package tech.metavm.flow.rest;

import tech.metavm.dto.RefDTO;
import tech.metavm.object.instance.rest.FieldValue;

import javax.annotation.Nullable;

public record InputFieldDTO (
        RefDTO fieldRef,
        String name,
        RefDTO typeRef,
        FieldValue defaultValue,
        @Nullable ValueDTO condition
) implements FieldReferringDTO<InputFieldDTO> {

    public InputFieldDTO copyWithFieldRef(RefDTO fieldRef) {
        return new InputFieldDTO(fieldRef, name, typeRef, defaultValue, condition);
    }

}
