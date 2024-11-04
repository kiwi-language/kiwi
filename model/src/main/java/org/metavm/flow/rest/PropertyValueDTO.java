package org.metavm.flow.rest;

import org.metavm.object.type.rest.dto.PropertyRefDTO;

public record PropertyValueDTO(PropertyRefDTO propertyRef) implements ValueDTO {
    @Override
    public int getKind() {
        return ValueKindCodes.PROPERTY;
    }
}
