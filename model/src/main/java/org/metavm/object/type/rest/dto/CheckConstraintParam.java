package org.metavm.object.type.rest.dto;

import org.metavm.flow.rest.ValueDTO;

public record CheckConstraintParam(ValueDTO value) implements ConstraintParam {

    public int getKind() {
        return ConstraintKindCodes.CHECK;
    }

}
