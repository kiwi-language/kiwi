package tech.metavm.flow.rest;

import tech.metavm.dto.RefDTO;
import tech.metavm.object.instance.rest.FieldValueDTO;

public record InputFieldDTO (
        RefDTO fieldRef,
        String name,
        RefDTO typeRef,
        FieldValueDTO defaultValue
) implements FieldReferringDTO<InputFieldDTO> {

    public InputFieldDTO copyWithFieldRef(RefDTO fieldRef) {
        return new InputFieldDTO(fieldRef, name, typeRef, defaultValue);
    }

}
