package org.metavm.flow.rest;

import org.metavm.object.instance.rest.FieldValue;

public record ConstantValueDTO (
        FieldValue value
) implements ValueDTO{
    @Override
    public int getKind() {
        return ValueKindCodes.CONSTANT;
    }

}
