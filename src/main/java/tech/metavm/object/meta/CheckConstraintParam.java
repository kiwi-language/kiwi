package tech.metavm.object.meta;

import tech.metavm.flow.rest.ValueDTO;

public record CheckConstraintParam(
        ValueDTO value
) {

    public int getKind() {
        return ConstraintKind.CHECK.code();
    }

}
