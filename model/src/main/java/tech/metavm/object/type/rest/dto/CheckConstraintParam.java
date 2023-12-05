package tech.metavm.object.type.rest.dto;

import tech.metavm.flow.rest.ValueDTO;

public record CheckConstraintParam(ValueDTO value) implements ConstraintParam {

    public int getKind() {
        return ConstraintKindCodes.CHECK;
    }

}
