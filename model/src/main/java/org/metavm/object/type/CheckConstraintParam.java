package org.metavm.object.type;

import org.metavm.flow.rest.ValueDTO;
import org.metavm.object.type.rest.dto.ConstraintParam;

public record CheckConstraintParam(
        ValueDTO value
) implements ConstraintParam {

    public int getKind() {
        return ConstraintKind.CHECK.code();
    }

}
