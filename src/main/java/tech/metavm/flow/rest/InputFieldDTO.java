package tech.metavm.flow.rest;

import tech.metavm.dto.RefDTO;
import tech.metavm.object.instance.rest.FieldValue;

public record InputFieldDTO (
        RefDTO fieldRef,
        String name,
        RefDTO typeRef,
        FieldValue defaultValue
) implements FieldReferringDTO<InputFieldDTO> {

    public InputFieldDTO copyWithFieldRef(RefDTO fieldRef) {
        return new InputFieldDTO(fieldRef, name, typeRef, defaultValue);
    }

}
